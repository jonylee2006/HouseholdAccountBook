package com.lazyledger.subscription.controller;

import com.lazyledger.common.model.ApiResponse;
import com.lazyledger.security.CurrentUserService;
import com.lazyledger.security.UserPrincipal;
import com.lazyledger.ledger.service.LedgerAccessService;
import com.lazyledger.subscription.domain.Subscription;
import com.lazyledger.subscription.service.SubscriptionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/v1/subscription")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final CurrentUserService currentUserService;
    private final LedgerAccessService ledgerAccessService;

    public SubscriptionController(SubscriptionService subscriptionService,
                                  CurrentUserService currentUserService,
                                  LedgerAccessService ledgerAccessService) {
        this.subscriptionService = subscriptionService;
        this.currentUserService = currentUserService;
        this.ledgerAccessService = ledgerAccessService;
    }

    @PostMapping
    public ApiResponse<Subscription> upsert(@Valid @RequestBody SubscriptionRequest request) {
        UserPrincipal user = currentUserService.currentUser();
        ledgerAccessService.ensureWritePermission(request.ledgerId(), user.userId());
        Subscription subscription = subscriptionService.upsert(request.ledgerId(), request.level(), request.expiresAt());
        return ApiResponse.ok(subscription);
    }

    public record SubscriptionRequest(@NotNull Long ledgerId,
                                      @NotBlank String level,
                                      OffsetDateTime expiresAt) {
    }
}
