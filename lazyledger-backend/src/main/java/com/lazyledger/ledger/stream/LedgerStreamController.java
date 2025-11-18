package com.lazyledger.ledger.stream;

import com.lazyledger.security.CurrentUserService;
import com.lazyledger.security.UserPrincipal;
import com.lazyledger.ledger.service.LedgerAccessService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/v1/ledger")
public class LedgerStreamController {

    private final LedgerStreamService ledgerStreamService;
    private final LedgerAccessService ledgerAccessService;
    private final CurrentUserService currentUserService;

    public LedgerStreamController(LedgerStreamService ledgerStreamService,
                                  LedgerAccessService ledgerAccessService,
                                  CurrentUserService currentUserService) {
        this.ledgerStreamService = ledgerStreamService;
        this.ledgerAccessService = ledgerAccessService;
        this.currentUserService = currentUserService;
    }

    @GetMapping(value = "/{ledgerId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@PathVariable Long ledgerId) {
        UserPrincipal user = currentUserService.currentUser();
        ledgerAccessService.ensureMember(ledgerId, user.userId());
        return ledgerStreamService.subscribe(ledgerId);
    }
}
