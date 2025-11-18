package com.lazyledger.subscription.service;

import com.lazyledger.subscription.domain.Subscription;
import com.lazyledger.subscription.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @Transactional
    public Subscription upsert(Long ledgerId, String level, OffsetDateTime expiresAt) {
        Subscription subscription = subscriptionRepository.findByLedgerId(ledgerId)
                .orElseGet(Subscription::new);
        subscription.setLedgerId(ledgerId);
        subscription.setLevel(level);
        subscription.setExpiresAt(expiresAt);
        subscription.setUpdatedAt(OffsetDateTime.now());
        return subscriptionRepository.save(subscription);
    }

    public Subscription findByLedgerId(Long ledgerId) {
        return subscriptionRepository.findByLedgerId(ledgerId).orElse(null);
    }
}
