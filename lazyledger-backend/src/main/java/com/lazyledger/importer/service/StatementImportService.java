package com.lazyledger.importer.service;

import com.lazyledger.common.enums.ImportJobStatus;
import com.lazyledger.common.enums.ImportSourceType;
import com.lazyledger.common.exception.BusinessException;
import com.lazyledger.common.enums.TransactionDirection;
import com.lazyledger.importer.dto.ImportJobResponse;
import com.lazyledger.importer.dto.ImportResultResponse;
import com.lazyledger.importer.dto.ImportedTransactionDto;
import com.lazyledger.importer.dto.StatementImportRequest;
import com.lazyledger.importer.model.StatementRecord;
import com.lazyledger.importer.parser.StatementParser;
import com.lazyledger.importer.parser.StatementParserFactory;
import com.lazyledger.ledger.domain.ImportJob;
import com.lazyledger.ledger.domain.Ledger;
import com.lazyledger.ledger.domain.Transaction;
import com.lazyledger.ledger.repository.ImportJobRepository;
import com.lazyledger.ledger.repository.LedgerRepository;
import com.lazyledger.ledger.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatementImportService {

    private static final Logger log = LoggerFactory.getLogger(StatementImportService.class);

    private final LedgerRepository ledgerRepository;
    private final ImportJobRepository importJobRepository;
    private final TransactionRepository transactionRepository;
    private final StatementParserFactory parserFactory;

    public StatementImportService(LedgerRepository ledgerRepository,
                                  ImportJobRepository importJobRepository,
                                  TransactionRepository transactionRepository,
                                  StatementParserFactory parserFactory) {
        this.ledgerRepository = ledgerRepository;
        this.importJobRepository = importJobRepository;
        this.transactionRepository = transactionRepository;
        this.parserFactory = parserFactory;
    }

    @Transactional
    public ImportResultResponse importStatement(StatementImportRequest request, MultipartFile file) {
        Ledger ledger = ledgerRepository.findById(request.ledgerId())
                .orElseThrow(() -> new BusinessException("LEDGER_NOT_FOUND", "账本不存在"));
        ImportJob job = createJob(request.sourceType(), ledger.getId());
        try (InputStream inputStream = file.getInputStream()) {
            StatementParser parser = parserFactory.getParser(request.sourceType());
            List<StatementRecord> records = parser.parse(inputStream);
            job.setStatus(ImportJobStatus.PROCESSING);
            job.setTotalCount(records.size());
            List<Transaction> transactions = records.stream()
                    .map(record -> toTransaction(record, request, job.getId()))
                    .collect(Collectors.toList());

            if (!request.dryRun()) {
                transactionRepository.saveAll(transactions);
            }
            job.setSuccessCount(transactions.size());
            job.setFailureCount(0);
            job.setStatus(ImportJobStatus.COMPLETED);
            job.setCompletedAt(OffsetDateTime.now());
            importJobRepository.save(job);
            List<ImportedTransactionDto> preview = transactions.stream()
                    .limit(20)
                    .map(this::toPreview)
                    .toList();
            return new ImportResultResponse(ImportJobResponse.from(job), preview);
        } catch (IOException e) {
            log.error("读取账单失败", e);
            failJob(job, e.getMessage());
            throw new BusinessException("IMPORT_FAILED", "读取账单文件失败");
        } catch (Exception ex) {
            log.error("导入账单失败", ex);
            failJob(job, ex.getMessage());
            throw new BusinessException("IMPORT_FAILED", ex.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public ImportJobResponse findJob(Long jobId) {
        ImportJob job = importJobRepository.findById(jobId)
                .orElseThrow(() -> new BusinessException("JOB_NOT_FOUND", "导入任务不存在"));
        return ImportJobResponse.from(job);
    }

    @Transactional(readOnly = true)
    public List<ImportedTransactionDto> findTransactionsByJob(Long jobId) {
        return transactionRepository.findByImportJobId(jobId).stream()
                .map(this::toPreview)
                .toList();
    }

    private ImportJob createJob(ImportSourceType sourceType, Long ledgerId) {
        ImportJob job = new ImportJob();
        job.setLedgerId(ledgerId);
        job.setSourceType(sourceType);
        job.setStatus(ImportJobStatus.PENDING);
        return importJobRepository.save(job);
    }

    private void failJob(ImportJob job, String message) {
        job.setStatus(ImportJobStatus.FAILED);
        job.setErrorMessage(message);
        job.setCompletedAt(OffsetDateTime.now());
        importJobRepository.save(job);
    }

    private Transaction toTransaction(StatementRecord record, StatementImportRequest request, Long jobId) {
        if (record.occurredAt() == null) {
            throw new BusinessException("INVALID_RECORD", "账单缺少交易时间");
        }
        Transaction transaction = new Transaction();
        transaction.setLedgerId(request.ledgerId());
        transaction.setImportJobId(jobId);
        transaction.setSourceType(request.sourceType());
        transaction.setOccurredAt(record.occurredAt());
        transaction.setMerchantName(record.merchantName());
        transaction.setSubject(record.subject());
        transaction.setAmount(record.amount());
        transaction.setDirection(record.direction() == null ? TransactionDirection.EXPENSE : record.direction());
        transaction.setPaymentMethod(record.paymentMethod());
        transaction.setReferenceId(record.referenceId());
        transaction.setRawPayload(record.rawLine());
        return transaction;
    }

    private ImportedTransactionDto toPreview(Transaction transaction) {
        return new ImportedTransactionDto(
                transaction.getOccurredAt(),
                transaction.getMerchantName(),
                transaction.getSubject(),
                transaction.getAmount(),
                transaction.getDirection(),
                transaction.getPaymentMethod()
        );
    }
}
