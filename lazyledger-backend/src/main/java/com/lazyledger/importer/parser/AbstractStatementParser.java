package com.lazyledger.importer.parser;

import com.lazyledger.common.enums.TransactionDirection;
import com.lazyledger.importer.model.StatementRecord;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class AbstractStatementParser implements StatementParser {

    private static final Charset FALLBACK_CHARSET = Charset.forName("GB18030");
    private static final List<DateTimeFormatter> DEFAULT_FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    );

    protected List<String> readMeaningfulLines(InputStream inputStream) {
        byte[] bytes;
        try {
            bytes = inputStream.readAllBytes();
        } catch (IOException e) {
            throw new IllegalStateException("读取账单文件失败", e);
        }
        String content = decode(bytes, StandardCharsets.UTF_8)
                .orElseGet(() -> decode(bytes, FALLBACK_CHARSET).orElse(""));
        content = content.replace('\ufeff', ' ').trim();
        return Arrays.stream(content.split("\\r?\\n"))
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .collect(Collectors.toList());
    }

    private Optional<String> decode(byte[] bytes, Charset charset) {
        try {
            return Optional.of(new String(bytes, charset));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    protected CSVParser buildCsvParser(List<String> lines, int headerLineIndex) {
        String headerLine = lines.get(headerLineIndex);
        String[] headers = Arrays.stream(headerLine.split(","))
                .map(header -> header.replace("\"", "").trim())
                .toArray(String[]::new);
        String csvContent = lines.stream()
                .skip(headerLineIndex + 1L)
                .collect(Collectors.joining("\n"));
        InputStream csvStream = new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8));
        Reader reader = new InputStreamReader(csvStream, StandardCharsets.UTF_8);
        try {
            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setTrim(true)
                    .setIgnoreSurroundingSpaces(true)
                    .setIgnoreEmptyLines(true)
                    .setAllowMissingColumnNames(true)
                    .setSkipHeaderRecord(false)
                    .setNullString("")
                    .setHeader(headers)
                    .build();
            return new CSVParser(reader, format);
        } catch (IOException e) {
            throw new IllegalStateException("解析 CSV 内容失败", e);
        }
    }

    protected LocalDateTime parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        for (DateTimeFormatter formatter : DEFAULT_FORMATTERS) {
            try {
                return LocalDateTime.parse(normalized, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }
        throw new IllegalArgumentException("无法解析时间: " + value);
    }

    protected BigDecimal parseAmount(String value) {
        if (value == null || value.isBlank()) {
            return BigDecimal.ZERO;
        }
        String normalized = value.replace("¥", "").replace(",", "").trim();
        if (normalized.startsWith("+")) {
            normalized = normalized.substring(1);
        }
        if (normalized.endsWith("元")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return new BigDecimal(normalized);
    }

    protected TransactionDirection parseDirection(String value, BigDecimal amount) {
        if (value != null) {
            String normalized = value.replace("收/支", "").trim().toLowerCase(Locale.ROOT);
            if (normalized.contains("支")) {
                return TransactionDirection.EXPENSE;
            }
            if (normalized.contains("收") || normalized.contains("入")) {
                return TransactionDirection.INCOME;
            }
        }
        return amount.compareTo(BigDecimal.ZERO) >= 0 ? TransactionDirection.INCOME : TransactionDirection.EXPENSE;
    }

    protected StatementRecord toRecord(LocalDateTime occurredAt,
                                       String merchantName,
                                       String subject,
                                       BigDecimal amount,
                                       TransactionDirection direction,
                                       String paymentMethod,
                                       String referenceId,
                                       String rawLine) {
        return new StatementRecord(occurredAt, merchantName, subject, amount.abs(), direction, paymentMethod, referenceId, rawLine);
    }

    protected List<StatementRecord> collectRecords(CSVParser parser) {
        List<StatementRecord> records = new ArrayList<>();
        for (CSVRecord csvRecord : parser) {
            StatementRecord record = mapRecord(csvRecord);
            if (record != null) {
                records.add(record);
            }
        }
        return records;
    }

    protected abstract StatementRecord mapRecord(CSVRecord csvRecord);

    protected String get(CSVRecord record, String header) {
        return record.isMapped(header) ? record.get(header) : null;
    }

    protected String raw(CSVRecord record) {
        return String.join(",", record.toList());
    }
}
