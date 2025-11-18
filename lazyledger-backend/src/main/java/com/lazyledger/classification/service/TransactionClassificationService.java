package com.lazyledger.classification.service;

import com.lazyledger.classification.domain.ClassificationRule;
import com.lazyledger.classification.repository.ClassificationRuleRepository;
import com.lazyledger.external.ClassificationServiceClient;
import com.lazyledger.ledger.domain.Transaction;
import com.lazyledger.ledger.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class TransactionClassificationService {

    private static final Logger log = LoggerFactory.getLogger(TransactionClassificationService.class);

    private final TransactionRepository transactionRepository;
    private final ClassificationRuleRepository classificationRuleRepository;
    private final ClassificationDictionary classificationDictionary;
    private final ClassificationServiceClient classificationServiceClient;

    public TransactionClassificationService(TransactionRepository transactionRepository,
                                            ClassificationRuleRepository classificationRuleRepository,
                                            ClassificationDictionary classificationDictionary,
                                            ClassificationServiceClient classificationServiceClient) {
        this.transactionRepository = transactionRepository;
        this.classificationRuleRepository = classificationRuleRepository;
        this.classificationDictionary = classificationDictionary;
        this.classificationServiceClient = classificationServiceClient;
    }

    @Transactional
    public List<Long> classify(Long ledgerId, List<Long> transactionIds) {
        List<Transaction> transactions = transactionRepository.findAllById(transactionIds);
        List<ClassificationRule> rules = classificationRuleRepository.findByLedgerId(ledgerId);
        List<Long> remaining = new ArrayList<>();
        List<Transaction> updated = new ArrayList<>();
        for (Transaction transaction : transactions) {
            if (transaction.getCategory() != null && !transaction.getCategory().isBlank()) {
                continue;
            }
            Optional<String> category = matchRule(transaction, rules)
                    .or(() -> classificationDictionary.match(transaction.getMerchantName()));
            if (category.isPresent()) {
                transaction.setCategory(category.get());
                updated.add(transaction);
            } else {
                remaining.add(transaction.getId());
            }
        }
        if (!updated.isEmpty()) {
            transactionRepository.saveAll(updated);
        }
        if (!remaining.isEmpty()) {
            log.info("{} transactions pending external classification", remaining.size());
            classificationServiceClient.trigger(ledgerId, null, remaining);
        }
        return remaining;
    }

    @Transactional
    public void updateCategory(Transaction transaction, String category) {
        transaction.setCategory(category);
        transactionRepository.save(transaction);
    }

    @Transactional
    public void saveRule(Long ledgerId, Long userId, String keyword, String category) {
        if (keyword == null || keyword.isBlank()) {
            return;
        }
        String normalized = keyword.toLowerCase(Locale.ROOT);
        ClassificationRule rule = classificationRuleRepository
                .findByLedgerIdAndKeywordIgnoreCase(ledgerId, normalized)
                .orElseGet(ClassificationRule::new);
        rule.setLedgerId(ledgerId);
        rule.setKeyword(normalized);
        rule.setCategory(category);
        rule.setCreatedBy(userId);
        classificationRuleRepository.save(rule);
    }

    private Optional<String> matchRule(Transaction transaction, List<ClassificationRule> rules) {
        if (transaction.getMerchantName() == null) {
            return Optional.empty();
        }
        String merchantLower = transaction.getMerchantName().toLowerCase(Locale.ROOT);
        return rules.stream()
                .filter(rule -> merchantLower.contains(rule.getKeyword().toLowerCase(Locale.ROOT)))
                .map(ClassificationRule::getCategory)
                .findFirst();
    }
}
