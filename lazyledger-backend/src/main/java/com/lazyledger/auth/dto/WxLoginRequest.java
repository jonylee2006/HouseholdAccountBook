package com.lazyledger.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record WxLoginRequest(@NotBlank String code,
                             String phone,
                             String nickname) {
}
