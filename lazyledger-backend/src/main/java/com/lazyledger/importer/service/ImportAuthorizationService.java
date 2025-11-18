package com.lazyledger.importer.service;

import com.lazyledger.importer.dto.ImportAuthorizationRequest;
import com.lazyledger.importer.dto.ImportAuthorizationResponse;
import com.lazyledger.ledger.domain.ImportAuthorization;
import com.lazyledger.ledger.domain.Ledger;
import com.lazyledger.ledger.repository.ImportAuthorizationRepository;
import com.lazyledger.ledger.repository.LedgerRepository;
import com.lazyledger.ledger.service.LedgerAccessService;
import com.lazyledger.security.CurrentUserService;
import com.lazyledger.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ImportAuthorizationService {

    private final ImportAuthorizationRepository authorizationRepository;
    private final LedgerRepository ledgerRepository;
    private final LedgerAccessService ledgerAccessService;
    private final CurrentUserService currentUserService;

    public ImportAuthorizationService(ImportAuthorizationRepository authorizationRepository,
                                      LedgerRepository ledgerRepository,
                                      LedgerAccessService ledgerAccessService,
                                      CurrentUserService currentUserService) {
        this.authorizationRepository = authorizationRepository;
        this.ledgerRepository = ledgerRepository;
        this.ledgerAccessService = ledgerAccessService;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public ImportAuthorizationResponse create(ImportAuthorizationRequest request) {
        Ledger ledger = ledgerRepository.findById(request.ledgerId())
                .orElseThrow(() -> new IllegalArgumentException("账本不存在"));
        UserPrincipal user = currentUserService.currentUser();
        ledgerAccessService.ensureWritePermission(ledger.getId(), user.userId());
        ImportAuthorization authorization = new ImportAuthorization();
        authorization.setLedgerId(ledger.getId());
        authorization.setSourceType(request.sourceType());
        authorization.setDisplayName(request.displayName());
        authorization.setAccessToken(request.accessToken());
        authorization.setRefreshToken(request.refreshToken());
        authorization.setExpiresAt(request.expiresAt());
        return toResponse(authorizationRepository.save(authorization));
    }

    @Transactional(readOnly = true)
    public List<ImportAuthorizationResponse> list(Long ledgerId) {
        UserPrincipal user = currentUserService.currentUser();
        ledgerAccessService.ensureWritePermission(ledgerId, user.userId());
        return authorizationRepository.findByLedgerId(ledgerId).stream()
                .map(this::toResponse)
                .toList();
    }

    private ImportAuthorizationResponse toResponse(ImportAuthorization authorization) {
        return new ImportAuthorizationResponse(
                authorization.getId(),
                authorization.getLedgerId(),
                authorization.getSourceType(),
                authorization.getDisplayName(),
                authorization.getExpiresAt()
        );
    }
}
