package com.lazyledger.category.repository;

import com.lazyledger.category.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByLedgerIdIsNullOrLedgerId(Long ledgerId);
}
