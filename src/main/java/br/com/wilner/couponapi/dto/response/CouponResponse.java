package br.com.wilner.couponapi.dto.response;

import br.com.wilner.couponapi.domain.enums.CouponStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CouponResponse(
        UUID id,
        String code,
        String description,
        BigDecimal discountValue,
        OffsetDateTime expirationDate,
        CouponStatus status,
        boolean published,
        boolean redeemed
) {
}