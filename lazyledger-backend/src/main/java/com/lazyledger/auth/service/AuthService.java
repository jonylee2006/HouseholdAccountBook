package com.lazyledger.auth.service;

import com.lazyledger.auth.dto.WxLoginRequest;
import com.lazyledger.auth.dto.WxLoginResponse;
import com.lazyledger.user.domain.UserAccount;
import com.lazyledger.user.repository.UserAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserAccountRepository userAccountRepository;

    public AuthService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    public WxLoginResponse wxLogin(WxLoginRequest request) {
        // 在真实环境下，这里需要通过微信服务端校验 code 并获取 openid
        String pseudoPhone = request.phone() != null ? request.phone() : ("wx_" + request.code());
        UserAccount user = userAccountRepository.findByPhone(pseudoPhone)
                .orElseGet(() -> {
                    UserAccount account = new UserAccount();
                    account.setPhone(pseudoPhone);
                    account.setNickname(request.nickname() != null ? request.nickname() : "微信用户");
                    account.setCreatedAt(OffsetDateTime.now());
                    return userAccountRepository.save(account);
                });
        if (request.nickname() != null) {
            user.setNickname(request.nickname());
        }
        userAccountRepository.save(user);
        String token = user.getId() + ":" + UUID.randomUUID();
        log.debug("Generated pseudo token {} for user {}", token, user.getId());
        return new WxLoginResponse(user.getId(), token, user.getNickname());
    }
}
