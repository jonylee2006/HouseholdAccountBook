package com.lazyledger.importer.service;

import com.lazyledger.common.enums.ImportJobStatus;
import com.lazyledger.common.enums.TransactionDirection;
import com.lazyledger.common.exception.BusinessException;
import com.lazyledger.importer.event.StatementImportedEvent;
import com.lazyledger.importer.model.StatementRecord;
import com.lazyledger.importer.parser.StatementParser;
import com.lazyledger.importer.parser.StatementParserFactory;
import com.lazyledger.importer.queue.ImportTaskMessage;
import com.lazyledger.importer.remote.RemoteStatementClient;
import com.lazyledger.ledger.domain.ImportAuthorization;
import com.lazyledger.ledger.domain.ImportJob;
import com.lazyledger.ledger.domain.Transaction;
import com.lazyledger.ledger.repository.ImportAuthorizationRepository;
import com.lazyledger.ledger.repository.ImportJobRepository;
import com.lazyledger.ledger.repository.TransactionRepository;
import com.lazyledger.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ImportJobProcessor {

    private static final Logger log = LoggerFactory.getLogger(ImportJobProcessor.class);

    private final ImportJobRepository importJobRepository;
    private final TransactionRepository transactionRepository;
    private final StatementParserFactory parserFactory;
    private final StorageService storageService;
    private final RemoteStatementClient remoteStatementClient;
    private final ImportAuthorizationRepository authorizationRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ImportJobProcessor(ImportJobRepository importJobRepository,
                              TransactionRepository transactionRepository,
                              StatementParserFactory parserFactory,
                              StorageService storageService,
                              RemoteStatementClient remoteStatementClient,
                              ImportAuthorizationRepository authorizationRepository,
                              ApplicationEventPublisher eventPublisher) {
        this.importJobRepository = importJobRepository;
        this.transactionRepository = transactionRepository;
        this.parserFactory = parserFactory;
        this.storageService = storageService;
        this.remoteStatementClient = remoteStatementClient;
        this.authorizationRepository = authorizationRepository;
        this.eventPublisher = eventPublisher;
    }

    public void process(ImportTaskMessage message) {
        ImportJob job = importJobRepository.findById(message.jobId())
                .orElseThrow(() -> new BusinessException("JOB_NOT_FOUND", "导入任务不存在"));
        if (job.getStatus() == ImportJobStatus.COMPLETED) {
            log.info("Job {} already completed", job.getId());
            return;
        }
        job.setStatus(ImportJobStatus.PROCESSING);
        importJobRepository.save(job);
        try (InputStream inputStream = openInputStream(job)) {
            StatementParser parser = parserFactory.getParser(job.getSourceType());
            List<StatementRecord> records = parser.parse(inputStream);
            job.setTotalCount(records.size());
            List<Transaction> transactions = records.stream()
                    .map(record -> toTransaction(record, job))
                    .collect(Collectors.toList());
            List<Transaction> saved = transactionRepository.saveAll(transactions);
            job.setSuccessCount(saved.size());
            job.setFailureCount(0);
            job.setStatus(ImportJobStatus.COMPLETED);
            job.setCompletedAt(OffsetDateTime.now());
            importJobRepository.save(job);
            eventPublisher.publishEvent(new StatementImportedEvent(this,
                    job.getLedgerId(),
                    job.getId(),
                    saved.stream().map(Transaction::getId).toList()));
            log.info("Job {} completed with {} records", job.getId(), saved.size());
        } catch (Exception ex) {
            log.error("Job {} failed", job.getId(), ex);
            job.setStatus(ImportJobStatus.FAILED);
            job.setErrorMessage(ex.getMessage());
            job.setCompletedAt(OffsetDateTime.now());
            importJobRepository.save(job);
        }
    }

    private InputStream openInputStream(ImportJob job) {
        if (job.getObjectKey() != null) {
            return storageService.openStream(job.getObjectKey());
        }
        if (job.getAuthorizationId() != null) {
            ImportAuthorization authorization = authorizationRepository.findById(job.getAuthorizationId())
                    .orElseThrow(() -> new BusinessException("AUTH_NOT_FOUND", "授权不存在"));
            return remoteStatementClient.fetch(authorization, job.getStatementDate());
        }
        throw new BusinessException("JOB_INPUT_NOT_FOUND", "任务缺少导入源");
    }

    private Transaction toTransaction(StatementRecord record, ImportJob job) {
        if (record.occurredAt() == null) {
            throw new BusinessException("INVALID_RECORD", "账单缺少交易时间");
        }
        Transaction transaction = new Transaction();
        transaction.setLedgerId(job.getLedgerId());
        transaction.setImportJobId(job.getId());
        transaction.setSourceType(job.getSourceType());
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
}
