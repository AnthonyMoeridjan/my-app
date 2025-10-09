package com.cofeecode.application.powerhauscore.services.ai;

import com.cofeecode.application.powerhauscore.data.TransactionType;
import com.fasterxml.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Represents the structured information returned by a receipt extraction service.
 */
public class ReceiptExtractionResult {

    private LocalDate date;
    private BigDecimal amount;
    private String currency;
    private BigDecimal vat;
    private String description;
    private String category;
    private String dagboek;
    private TransactionType transactionType;
    private String projectName;
    private final List<String> warnings = new ArrayList<>();

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getVat() {
        return vat;
    }

    public void setVat(BigDecimal vat) {
        this.vat = vat;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDagboek() {
        return dagboek;
    }

    public void setDagboek(String dagboek) {
        this.dagboek = dagboek;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public List<String> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }

    public void addWarning(String warning) {
        if (warning != null && !warning.isBlank()) {
            warnings.add(warning);
        }
    }

    public boolean hasAnyValue() {
        return date != null || amount != null || vat != null ||
                (description != null && !description.isBlank()) ||
                (category != null && !category.isBlank()) ||
                (dagboek != null && !dagboek.isBlank()) ||
                transactionType != null ||
                (projectName != null && !projectName.isBlank());
    }

    public static ReceiptExtractionResult fromJson(JsonNode root) {
        ReceiptExtractionResult result = new ReceiptExtractionResult();
        if (root == null || root.isNull()) {
            return result;
        }

        parseDate(root).ifPresent(result::setDate);
        parseAmount(root).ifPresent(pair -> {
            result.setAmount(pair.value());
            result.setCurrency(pair.currency());
        });
        parseVat(root).ifPresent(result::setVat);
        textValue(root, "description").ifPresent(result::setDescription);
        textValue(root, "category").ifPresent(result::setCategory);
        textValue(root, "dagboek").ifPresent(result::setDagboek);
        parseTransactionType(root).ifPresent(result::setTransactionType);
        parseProjectName(root).ifPresent(result::setProjectName);

        if (root.has("warnings") && root.get("warnings").isArray()) {
            for (JsonNode warningNode : root.get("warnings")) {
                if (warningNode.isTextual()) {
                    result.addWarning(warningNode.asText());
                }
            }
        }

        return result;
    }

    private record AmountCurrencyPair(BigDecimal value, String currency) {}

    private static Optional<LocalDate> parseDate(JsonNode root) {
        JsonNode dateNode = pickFirst(root, "date", "transactionDate", "issuedOn");
        if (dateNode != null && dateNode.isTextual()) {
            try {
                return Optional.of(LocalDate.parse(dateNode.asText()));
            } catch (DateTimeParseException ignored) {
                // Some providers return dd-MM-yyyy or other locale specific formats.
                // Try ISO first; custom formats can be parsed client-side if needed.
            }
        }
        return Optional.empty();
    }

    private static Optional<AmountCurrencyPair> parseAmount(JsonNode root) {
        JsonNode amountNode = pickFirst(root, "amount", "total", "grandTotal");
        if (amountNode == null || amountNode.isNull()) {
            return Optional.empty();
        }

        BigDecimal value = null;
        String currency = null;

        if (amountNode.isObject()) {
            JsonNode valueNode = pickFirst(amountNode, "value", "amount", "net");
            if (valueNode != null) {
                value = parseBigDecimal(valueNode).orElse(null);
            }
            JsonNode currencyNode = pickFirst(amountNode, "currency", "isoCurrency");
            if (currencyNode != null && currencyNode.isTextual()) {
                currency = currencyNode.asText();
            }
        } else {
            value = parseBigDecimal(amountNode).orElse(null);
        }

        if (currency == null) {
            currency = textValue(root, "currency").orElse(null);
        }

        if (value == null) {
            return Optional.empty();
        }

        return Optional.of(new AmountCurrencyPair(value, currency));
    }

    private static Optional<BigDecimal> parseVat(JsonNode root) {
        JsonNode vatNode = pickFirst(root, "vat", "btw", "taxAmount");
        if (vatNode == null || vatNode.isNull()) {
            return Optional.empty();
        }
        return parseBigDecimal(vatNode);
    }

    private static Optional<TransactionType> parseTransactionType(JsonNode root) {
        JsonNode typeNode = pickFirst(root, "transactionType", "type");
        if (typeNode != null && typeNode.isTextual()) {
            String type = typeNode.asText();
            if (!type.isBlank()) {
                try {
                    return Optional.of(TransactionType.valueOf(type.trim().toUpperCase(Locale.ROOT)));
                } catch (IllegalArgumentException ignored) {
                    // Unsupported type, ignore.
                }
            }
        }
        return Optional.empty();
    }

    private static Optional<String> parseProjectName(JsonNode root) {
        return textValue(root, "project")
                .or(() -> textValue(root, "projectName"))
                .or(() -> textValue(root, "project_name"));
    }

    private static Optional<String> textValue(JsonNode root, String field) {
        if (root.has(field) && root.get(field).isTextual()) {
            String text = root.get(field).asText();
            if (!text.isBlank()) {
                return Optional.of(text);
            }
        }
        return Optional.empty();
    }

    private static Optional<BigDecimal> parseBigDecimal(JsonNode node) {
        if (node.isNumber()) {
            return Optional.of(node.decimalValue());
        }
        if (node.isTextual()) {
            try {
                return Optional.of(new BigDecimal(node.asText().replaceAll(",", "")));
            } catch (NumberFormatException ignored) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    private static JsonNode pickFirst(JsonNode root, String... fieldNames) {
        for (String fieldName : fieldNames) {
            if (root.has(fieldName)) {
                JsonNode node = root.get(fieldName);
                if (!node.isNull()) {
                    return node;
                }
            }
        }
        return null;
    }
}

