package com.cofeecode.application.powerhauscore.controllers.api;

import com.cofeecode.application.powerhauscore.data.Project;
import com.cofeecode.application.powerhauscore.data.Transaction;
import com.cofeecode.application.powerhauscore.data.TransactionType;
import com.cofeecode.application.powerhauscore.dto.TransactionRequestDTO;
import com.cofeecode.application.powerhauscore.services.FileStorageService; // Import FileStorageService
import com.cofeecode.application.powerhauscore.services.ProjectService;
import com.cofeecode.application.powerhauscore.services.TransactionService;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import org.slf4j.Logger; // For logging
import org.slf4j.LoggerFactory; // For logging
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.persistence.criteria.Predicate;


@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    private final TransactionService transactionService;
    private final ProjectService projectService;
    private final FileStorageService fileStorageService; // Inject FileStorageService

    public TransactionController(TransactionService transactionService,
                                 ProjectService projectService,
                                 FileStorageService fileStorageService) { // Add to constructor
        this.transactionService = transactionService;
        this.projectService = projectService;
        this.fileStorageService = fileStorageService;
    }

    private Date convertToDateViaSqlDate(LocalDate dateToConvert) {
        return dateToConvert == null ? null : java.sql.Date.valueOf(dateToConvert);
    }

    private Specification<Transaction> buildTransactionSpecification(
            LocalDate startDate, LocalDate endDate, TransactionType type, Long projectId,
            String dagboek, String category, String description, String extra) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("date"), convertToDateViaSqlDate(startDate)));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("date"), convertToDateViaSqlDate(endDate)));
            }
            if (type != null) {
                predicates.add(cb.equal(root.get("transactionType"), type));
            }
            if (projectId != null) {
                predicates.add(cb.equal(root.get("project").get("id"), projectId));
            }
            if (dagboek != null && !dagboek.trim().isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("dagboek")), dagboek.trim().toLowerCase()));
            }
            if (category != null && !category.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("category")), "%" + category.trim().toLowerCase() + "%"));
            }
            if (description != null && !description.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("description")), "%" + description.trim().toLowerCase() + "%"));
            }
            if (extra != null && !extra.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("extra")), "%" + extra.trim().toLowerCase() + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @GetMapping
    @RolesAllowed({"USER", "RVC", "HR", "ADMIN"})
    public ResponseEntity<Page<Transaction>> getAllTransactions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String dagboek,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String extra,
            Pageable pageable) {
        Specification<Transaction> spec = buildTransactionSpecification(startDate, endDate, type, projectId, dagboek, category, description, extra);
        Page<Transaction> transactions = transactionService.list(pageable, spec);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/totals")
    @RolesAllowed({"USER", "RVC", "HR", "ADMIN"})
    public ResponseEntity<Map<String, BigDecimal>> getTransactionTotals(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) String dagboek,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String extra) {
        Specification<Transaction> spec = buildTransactionSpecification(startDate, endDate, type, projectId, dagboek, category, description, extra);
        List<Transaction> filteredTransactions = transactionService.findAll(spec);

        BigDecimal srdTotal = BigDecimal.ZERO;
        BigDecimal usdTotal = BigDecimal.ZERO;
        BigDecimal eurTotal = BigDecimal.ZERO;

        for (Transaction t : filteredTransactions) {
            if (t.getAmount() == null || t.getCurrency() == null) continue;
            BigDecimal amt = t.getTransactionType() == TransactionType.DEBIT ? t.getAmount().negate() : t.getAmount();
            switch (t.getCurrency().toUpperCase()) {
                case "SRD": srdTotal = srdTotal.add(amt); break;
                case "USD": usdTotal = usdTotal.add(amt); break;
                case "EUR": case "EURO": eurTotal = eurTotal.add(amt); break;
            }
        }
        return ResponseEntity.ok(Map.of("SRD", srdTotal, "USD", usdTotal, "EUR", eurTotal));
    }

    @GetMapping("/{id}")
    @RolesAllowed({"USER", "RVC", "HR", "ADMIN"})
    public ResponseEntity<Transaction> getTransactionById(@PathVariable Long id) {
        return transactionService.get(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @RolesAllowed({"USER", "HR", "ADMIN"})
    public ResponseEntity<Transaction> createTransaction(@Valid @RequestBody TransactionRequestDTO transactionDTO) {
        Transaction transaction = new Transaction();
        mapDtoToEntity(transactionDTO, transaction);
        Transaction createdTransaction = transactionService.create(transaction);
        return new ResponseEntity<>(createdTransaction, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @RolesAllowed({"USER", "HR", "ADMIN"})
    public ResponseEntity<Transaction> updateTransaction(@PathVariable Long id, @Valid @RequestBody TransactionRequestDTO transactionDTO) {
        Transaction existingTransaction = transactionService.get(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found with id: " + id));
        mapDtoToEntity(transactionDTO, existingTransaction);
        Transaction updatedTransaction = transactionService.update(existingTransaction);
        return ResponseEntity.ok(updatedTransaction);
    }

    @DeleteMapping("/{id}")
    @RolesAllowed({"ADMIN"})
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        Transaction transaction = transactionService.get(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found with id: " + id));

        // If transaction has a file path, attempt to delete the associated file first
        if (transaction.getFilePath() != null && !transaction.getFilePath().trim().isEmpty()) {
            try {
                boolean fileActuallyDeleted = fileStorageService.deleteFile(transaction.getFilePath());
                if (fileActuallyDeleted) {
                    logger.info("Successfully deleted attachment file: {} for transaction ID: {}", transaction.getFilePath(), id);
                } else {
                    // This might happen if file was already deleted, or permissions issue etc.
                    logger.warn("Could not delete attachment file: {} for transaction ID: {}. It might not exist or there was an error.", transaction.getFilePath(), id);
                }
            } catch (Exception e) {
                // Log error and continue to delete transaction record, or decide on stricter policy
                logger.error("Error deleting attachment file: {} for transaction ID: {}. Error: {}", transaction.getFilePath(), id, e.getMessage());
            }
        }
        transactionService.delete(id); // Delete transaction record
        return ResponseEntity.noContent().build();
    }

    // New Endpoint for deleting transaction attachment
    @DeleteMapping("/{transactionId}/attachment")
    @RolesAllowed({"USER", "HR", "ADMIN"}) // Align with edit permissions
    public ResponseEntity<Map<String, String>> deleteTransactionAttachment(@PathVariable Long transactionId) {
        Transaction transaction = transactionService.get(transactionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found with id: " + transactionId));

        String currentFilePath = transaction.getFilePath();

        if (currentFilePath == null || currentFilePath.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Transaction does not have an attachment to delete."));
        }

        try {
            boolean fileActuallyDeleted = fileStorageService.deleteFile(currentFilePath);
            if (fileActuallyDeleted) {
                logger.info("Successfully deleted attachment file: {}", currentFilePath);
            } else {
                // File might not exist on disk but path is in DB, or other error
                logger.warn("Could not delete attachment file: {}. It might not exist or there was an error.", currentFilePath);
                // Decide if this is an error for the client, or just a warning for server logs.
                // For now, we'll proceed to clear the path in DB.
            }
        } catch (Exception e) {
            logger.error("Error trying to delete physical file {}: {}", currentFilePath, e.getMessage());
            // Depending on policy, you might want to return an error here and not update the DB.
            // For this implementation, we will proceed to clear the DB path.
        }

        transaction.setFilePath(null); // Clear the file path
        transactionService.update(transaction); // Save the transaction with null filePath

        return ResponseEntity.ok(Map.of("message", "Attachment for transaction " + transactionId + " removed successfully."));
    }


    private void mapDtoToEntity(TransactionRequestDTO dto, Transaction entity) {
        // Ensure all fields from TransactionRequestDTO are mapped to Transaction entity
        entity.setDagboek(dto.getDagboek());
        if (dto.getDate() != null) {
             entity.setDate(convertToDateViaSqlDate(dto.getDate()));
        } else {
            entity.setDate(null);
        }
        entity.setCategory(dto.getCategory());
        entity.setDescription(dto.getDescription());
        entity.setAmount(dto.getAmount());
        entity.setBtw(dto.getBtw());
        entity.setCurrency(dto.getCurrency());
        entity.setTransactionType(dto.getTransactionType());
        entity.setFilePath(dto.getFilePath()); // filePath is now mapped
        entity.setExtra(dto.getExtra());

        if (dto.getProjectId() != null) {
            Project project = projectService.get(dto.getProjectId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid project ID: " + dto.getProjectId()));
            entity.setProject(project);
        } else {
            entity.setProject(null);
        }
    }

    // Make sure to copy ALL params for buildTransactionSpecification in getAllTransactions and getTransactionTotals
    // This was simplified in the prompt, ensure all params are passed, e.g.
    // Specification<Transaction> spec = buildTransactionSpecification(startDate, endDate, type, projectId, dagboek, category, description, extra);
}
