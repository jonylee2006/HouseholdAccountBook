package com.lazyledger.ledger.controller;

import com.lazyledger.common.model.ApiResponse;
import com.lazyledger.ledger.domain.Ledger;
import com.lazyledger.ledger.service.LedgerService;
import com.lazyledger.security.CurrentUserService;
import com.lazyledger.security.UserPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ledger")
public class LedgerController {

    private final LedgerService ledgerService;
    private final CurrentUserService currentUserService;

    public LedgerController(LedgerService ledgerService, CurrentUserService currentUserService) {
        this.ledgerService = ledgerService;
        this.currentUserService = currentUserService;
    }

    @PostMapping
    public ApiResponse<Ledger> create(@Valid @RequestBody CreateLedgerRequest request) {
        UserPrincipal user = currentUserService.currentUser();
        Ledger ledger = ledgerService.createLedger(request.name(), request.currency(), request.monthlyBudget(), user);
        return ApiResponse.ok(ledger);
    }

    public record CreateLedgerRequest(@NotBlank String name,
                                      String currency,
                                      Double monthlyBudget) {
    }
}
