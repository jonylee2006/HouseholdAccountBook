package com.lazyledger.transaction.controller;

import com.lazyledger.classification.service.TransactionClassificationService;
import com.lazyledger.common.model.ApiResponse;
import com.lazyledger.common.exception.BusinessException;
import com.lazyledger.ledger.domain.Transaction;
import com.lazyledger.ledger.repository.TransactionRepository;
import com.lazyledger.ledger.service.LedgerAccessService;
import com.lazyledger.security.CurrentUserService;
import com.lazyledger.security.UserPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/transactions")
public class TransactionController {

    private final TransactionRepository transactionRepository;
    private final LedgerAccessService ledgerAccessService;
    private final CurrentUserService currentUserService;
    private final TransactionClassificationService classificationService;

    public TransactionController(TransactionRepository transactionRepository,
                                 LedgerAccessService ledgerAccessService,
                                 CurrentUserService currentUserService,
                                 TransactionClassificationService classificationService) {
        this.transactionRepository = transactionRepository;
        this.ledgerAccessService = ledgerAccessService;
        this.currentUserService = currentUserService;
        this.classificationService = classificationService;
    }

    @PutMapping("/{transactionId}/category")
    public ApiResponse<Void> updateCategory(@PathVariable Long transactionId,
                                            @Valid @RequestBody UpdateCategoryRequest request) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new BusinessException("TRANSACTION_NOT_FOUND", "交易不存在"));
        UserPrincipal user = currentUserService.currentUser();
        ledgerAccessService.ensureWritePermission(transaction.getLedgerId(), user.userId());
        classificationService.updateCategory(transaction, request.category());
        if (request.saveRule()) {
            String keyword = request.keyword() == null || request.keyword().isBlank()
                    ? transaction.getMerchantName()
                    : request.keyword();
            classificationService.saveRule(transaction.getLedgerId(), user.userId(), keyword, request.category());
        }
        return ApiResponse.ok(null);
    }

    public record UpdateCategoryRequest(@NotBlank(message = "category 不能为空") String category,
                                        boolean saveRule,
                                        String keyword) {
    }
}
