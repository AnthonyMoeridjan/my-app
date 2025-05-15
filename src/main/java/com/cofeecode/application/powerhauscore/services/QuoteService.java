package com.cofeecode.application.powerhauscore.services;

import com.cofeecode.application.powerhauscore.data.Quote;
import com.cofeecode.application.powerhauscore.repository.QuoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class QuoteService {

    @Autowired
    private QuoteRepository quoteRepository;

    public List<Quote> listAll() {
        return quoteRepository.findAll();
    }

    public Optional<Quote> get(Long id) {
        return quoteRepository.findById(id);
    }

    public Quote save(Quote quote) {
        return quoteRepository.save(quote);
    }

    public void delete(Long id) {
        quoteRepository.deleteById(id);
    }
}