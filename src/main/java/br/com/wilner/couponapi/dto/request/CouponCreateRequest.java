package br.com.wilner.couponapi.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CouponCreateRequest(

        @Schema(example = "ABC-123", description = "Coupon code. Special characters are accepted and removed before persistence.")
        @NotBlank(message = "Coupon code is required")
        @Size(max = 50, message = "Coupon code must contain at most 50 characters before sanitization")
        String code,

        @Schema(example = "Black Friday coupon")
        @NotBlank(message = "Coupon description is required")
        @Size(max = 255, message = "Coupon description must contain at most 255 characters")
        String description,

        @Schema(example = "0.80")
        @NotNull(message = "Discount value is required")
        @DecimalMin(value = "0.5", message = "Discount value must be greater than or equal to 0.5")
        @Digits(integer = 8, fraction = 2, message = "Discount value must have up to 8 integer digits and 2 decimal places")
        BigDecimal discountValue,

        @Schema(example = "2030-11-04T17:14:45.180Z")
        @NotNull(message = "Expiration date is required")
        @FutureOrPresent(message = "Expiration date cannot be in the past")
        OffsetDateTime expirationDate,

        @Schema(example = "false", defaultValue = "false")
        Boolean published
) {
}