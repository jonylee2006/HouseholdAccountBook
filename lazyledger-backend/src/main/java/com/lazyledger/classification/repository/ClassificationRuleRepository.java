package com.lazyledger.classification.repository;

import com.lazyledger.classification.domain.ClassificationRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClassificationRuleRepository extends JpaRepository<ClassificationRule, Long> {

    List<ClassificationRule> findByLedgerId(Long ledgerId);

    Optional<ClassificationRule> findByLedgerIdAndKeywordIgnoreCase(Long ledgerId, String keyword);
}
