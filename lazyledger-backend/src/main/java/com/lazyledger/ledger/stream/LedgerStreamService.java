package com.lazyledger.ledger.stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class LedgerStreamService {

    private static final Logger log = LoggerFactory.getLogger(LedgerStreamService.class);
    private static final long DEFAULT_TIMEOUT = Duration.ofMinutes(30).toMillis();

    private final Map<Long, List<SseEmitter>> emitterMap = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long ledgerId) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        emitter.onCompletion(() -> removeEmitter(ledgerId, emitter));
        emitter.onTimeout(() -> removeEmitter(ledgerId, emitter));
        emitterMap.computeIfAbsent(ledgerId, key -> new CopyOnWriteArrayList<>()).add(emitter);
        try {
            emitter.send(SseEmitter.event().name("INIT").data("CONNECTED"));
        } catch (IOException e) {
            log.warn("Failed to send init event", e);
        }
        return emitter;
    }

    public void broadcast(Long ledgerId, Object payload) {
        List<SseEmitter> emitters = emitterMap.get(ledgerId);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }
        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event().name("LEDGER_EVENT").data(payload));
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
        });
    }

    private void removeEmitter(Long ledgerId, SseEmitter emitter) {
        List<SseEmitter> emitters = emitterMap.get(ledgerId);
        if (emitters != null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                emitterMap.remove(ledgerId);
            }
        }
    }
}
