package com.lazyledger.ledger.controller;

import com.lazyledger.common.model.ApiResponse;
import com.lazyledger.ledger.domain.LedgerInvite;
import com.lazyledger.ledger.service.LedgerInviteService;
import com.lazyledger.security.CurrentUserService;
import com.lazyledger.security.UserPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.OffsetDateTime;

@RestController
@RequestMapping("/v1/ledger")
public class LedgerInviteController {

    private final LedgerInviteService ledgerInviteService;
    private final CurrentUserService currentUserService;

    public LedgerInviteController(LedgerInviteService ledgerInviteService,
                                  CurrentUserService currentUserService) {
        this.ledgerInviteService = ledgerInviteService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/{ledgerId}/invite")
    public ApiResponse<InviteResponse> createInvite(@PathVariable Long ledgerId,
                                                    @Valid @RequestBody InviteRequest request) {
        UserPrincipal user = currentUserService.currentUser();
        Duration duration = Duration.ofMinutes(request.ttlMinutes());
        LedgerInvite invite = ledgerInviteService.createInvite(ledgerId, user, duration);
        return ApiResponse.ok(new InviteResponse(invite.getToken(), invite.getExpiredAt()));
    }

    @PostMapping("/invite/{token}/accept")
    public ApiResponse<Void> acceptInvite(@PathVariable String token) {
        UserPrincipal user = currentUserService.currentUser();
        ledgerInviteService.acceptInvite(token, user);
        return ApiResponse.ok(null);
    }

    public record InviteRequest(@Min(value = 10, message = "过期时间至少10分钟")
                                @Max(value = 1440, message = "过期时间不超过24小时")
                                Long expireMinutes) {
        public long ttlMinutes() {
            return expireMinutes == null ? 1440 : expireMinutes;
        }
    }

    public record InviteResponse(String token, OffsetDateTime expiredAt) {
    }
}
