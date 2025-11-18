package com.lazyledger.importer.service;

import com.lazyledger.common.enums.ImportJobStatus;
import com.lazyledger.common.enums.ImportSourceType;
import com.lazyledger.common.exception.BusinessException;
import com.lazyledger.importer.dto.ImportJobResponse;
import com.lazyledger.importer.dto.ImportedTransactionDto;
import com.lazyledger.importer.dto.StatementImportRequest;
import com.lazyledger.importer.dto.AuthorizationImportRequest;
import com.lazyledger.ledger.domain.ImportJob;
import com.lazyledger.ledger.domain.Ledger;
import com.lazyledger.ledger.domain.ImportAuthorization;
import com.lazyledger.ledger.repository.ImportJobRepository;
import com.lazyledger.ledger.repository.LedgerRepository;
import com.lazyledger.ledger.repository.TransactionRepository;
import com.lazyledger.ledger.repository.ImportAuthorizationRepository;
import com.lazyledger.ledger.service.LedgerAccessService;
import com.lazyledger.importer.queue.ImportJobPublisher;
import com.lazyledger.importer.queue.ImportTaskMessage;
import com.lazyledger.security.CurrentUserService;
import com.lazyledger.security.UserPrincipal;
import com.lazyledger.storage.StorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class StatementImportService {

    private final LedgerRepository ledgerRepository;
    private final ImportJobRepository importJobRepository;
    private final TransactionRepository transactionRepository;
    private final ImportAuthorizationRepository authorizationRepository;
    private final LedgerAccessService ledgerAccessService;
    private final StorageService storageService;
    private final CurrentUserService currentUserService;
    private final ImportJobPublisher importJobPublisher;

    public StatementImportService(LedgerRepository ledgerRepository,
                                  ImportJobRepository importJobRepository,
                                  TransactionRepository transactionRepository,
                                  ImportAuthorizationRepository authorizationRepository,
                                  LedgerAccessService ledgerAccessService,
                                  StorageService storageService,
                                  CurrentUserService currentUserService,
                                  ImportJobPublisher importJobPublisher) {
        this.ledgerRepository = ledgerRepository;
        this.importJobRepository = importJobRepository;
        this.transactionRepository = transactionRepository;
        this.authorizationRepository = authorizationRepository;
        this.ledgerAccessService = ledgerAccessService;
        this.storageService = storageService;
        this.currentUserService = currentUserService;
        this.importJobPublisher = importJobPublisher;
    }

    @Transactional
    public ImportJobResponse importStatement(StatementImportRequest request) {
        Ledger ledger = ledgerRepository.findById(request.ledgerId())
                .orElseThrow(() -> new BusinessException("LEDGER_NOT_FOUND", "账本不存在"));
        UserPrincipal user = currentUserService.currentUser();
        ledgerAccessService.ensureWritePermission(ledger.getId(), user.userId());
        ImportJob job = createJob(request.sourceType(), ledger.getId(), user.userId());
        job.setObjectKey(request.objectKey());
        job.setStatementDate(request.statementDate());
        try (var ignored = storageService.openStream(request.objectKey())) {
            // 仅用于校验对象是否存在
        } catch (Exception ex) {
            throw new BusinessException("OBJECT_NOT_FOUND", "未找到账单文件或无权限访问");
        }
        importJobRepository.save(job);
        publishJob(job);
        return ImportJobResponse.from(job);
    }

    @Transactional
    public ImportJobResponse importByAuthorization(AuthorizationImportRequest request) {
        ImportAuthorization authorization = authorizationRepository.findById(request.authorizationId())
                .orElseThrow(() -> new BusinessException("AUTH_NOT_FOUND", "授权不存在"));
        if (!authorization.getLedgerId().equals(request.ledgerId())) {
            throw new BusinessException("AUTH_MISMATCH", "授权与账本不匹配");
        }
        UserPrincipal user = currentUserService.currentUser();
        ledgerAccessService.ensureWritePermission(request.ledgerId(), user.userId());
        ImportJob job = createJob(authorization.getSourceType(), request.ledgerId(), user.userId());
        job.setAuthorizationId(authorization.getId());
        job.setStatementDate(request.statementDate());
        importJobRepository.save(job);
        publishJob(job);
        return ImportJobResponse.from(job);
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
                .map(transaction -> new ImportedTransactionDto(
                        transaction.getOccurredAt(),
                        transaction.getMerchantName(),
                        transaction.getSubject(),
                        transaction.getAmount(),
                        transaction.getDirection(),
                        transaction.getPaymentMethod()
                ))
                .toList();
    }

    private ImportJob createJob(ImportSourceType sourceType, Long ledgerId, Long requestedBy) {
        ImportJob job = new ImportJob();
        job.setLedgerId(ledgerId);
        job.setRequestedBy(requestedBy);
        job.setSourceType(sourceType);
        job.setStatus(ImportJobStatus.PENDING);
        return importJobRepository.save(job);
    }

    private void publishJob(ImportJob job) {
        job.setStatus(ImportJobStatus.QUEUED);
        importJobRepository.save(job);
        importJobPublisher.publish(new ImportTaskMessage(
                job.getId(),
                job.getLedgerId(),
                job.getSourceType(),
                job.getObjectKey(),
                job.getAuthorizationId(),
                job.getStatementDate()
        ));
    }

    private String buildObjectKey(ImportSourceType sourceType, Long ledgerId) {
        return sourceType.name().toLowerCase() + \"/\" + ledgerId + \"/\" + System.currentTimeMillis() + \".csv\";
    }
}
