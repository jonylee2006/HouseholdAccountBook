package com.lazyledger.classification.service;

import com.lazyledger.classification.domain.ClassificationRule;
import com.lazyledger.classification.repository.ClassificationRuleRepository;
import com.lazyledger.external.ClassificationServiceClient;
import com.lazyledger.ledger.domain.Transaction;
import com.lazyledger.ledger.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TransactionClassificationServiceTest {

    private TransactionRepository transactionRepository;
    private ClassificationRuleRepository classificationRuleRepository;
    private ClassificationDictionary classificationDictionary;
    private ClassificationServiceClient classificationServiceClient;
    private TransactionClassificationService service;

    @BeforeEach
    void setUp() {
        transactionRepository = mock(TransactionRepository.class);
        classificationRuleRepository = mock(ClassificationRuleRepository.class);
        classificationDictionary = new ClassificationDictionary();
        classificationServiceClient = mock(ClassificationServiceClient.class);
        service = new TransactionClassificationService(transactionRepository, classificationRuleRepository,
                classificationDictionary, classificationServiceClient);
    }

    @Test
    void shouldApplyRulesAndDictionary() {
        Transaction ruleMatch = new Transaction();
        ruleMatch.setId(1L);
        ruleMatch.setMerchantName("张三奶茶店");
        Transaction dictMatch = new Transaction();
        dictMatch.setId(2L);
        dictMatch.setMerchantName("饿了么外卖");
        Transaction noMatch = new Transaction();
        noMatch.setId(3L);
        noMatch.setMerchantName("未知商户");

        when(transactionRepository.findAllById(List.of(1L, 2L, 3L))).thenReturn(List.of(ruleMatch, dictMatch, noMatch));
        ClassificationRule rule = new ClassificationRule();
        rule.setKeyword("奶茶");
        rule.setCategory("饮品");
        when(classificationRuleRepository.findByLedgerId(10L)).thenReturn(List.of(rule));

        List<Long> remaining = service.classify(10L, List.of(1L, 2L, 3L));

        ArgumentCaptor<List<Transaction>> savedCaptor = ArgumentCaptor.forClass(List.class);
        verify(transactionRepository).saveAll(savedCaptor.capture());
        assertThat(savedCaptor.getValue()).hasSize(2);
        assertThat(ruleMatch.getCategory()).isEqualTo("饮品");
        assertThat(dictMatch.getCategory()).isEqualTo("餐饮");
        assertThat(remaining).containsExactly(3L);
        verify(classificationServiceClient).trigger(10L, null, List.of(3L));
    }

    @Test
    void shouldSaveRuleOnDemand() {
        service.saveRule(10L, 1L, "奶茶", "饮品");
        verify(classificationRuleRepository).save(any(ClassificationRule.class));
    }
}
