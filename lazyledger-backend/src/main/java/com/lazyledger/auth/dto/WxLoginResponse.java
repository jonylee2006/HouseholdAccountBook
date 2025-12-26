package com.lazyledger.auth.dto;

public record WxLoginResponse(Long userId,
                              String token,
                              String nickname) {
}
