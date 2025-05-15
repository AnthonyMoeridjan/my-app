package com.cofeecode.application.powerhauscore.repository;

import com.cofeecode.application.powerhauscore.data.Quote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuoteRepository extends JpaRepository<Quote, Long> {
}
