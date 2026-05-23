package br.com.wilner.couponapi.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CouponCreateRequest(

        @NotBlank(message = "Coupon code is required")
        String code,

        @NotBlank(message = "Coupon description is required")
        String description,

        @NotNull(message = "Discount value is required")
        @DecimalMin(value = "0.5", message = "Discount value must be greater than or equal to 0.5")
        BigDecimal discountValue,

        @NotNull(message = "Expiration date is required")
        @FutureOrPresent(message = "Expiration date cannot be in the past")
        OffsetDateTime expirationDate,

        Boolean published
) {
}