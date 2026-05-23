package br.com.wilner.couponapi.domain.model;

import br.com.wilner.couponapi.domain.enums.CouponStatus;
import br.com.wilner.couponapi.exception.BusinessException;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
public class Coupon {

    private static final int CODE_LENGTH = 6;
    private static final BigDecimal MINIMUM_DISCOUNT_VALUE = BigDecimal.valueOf(0.5);
    private static final String NON_ALPHANUMERIC_REGEX = "[^a-zA-Z0-9]";

    private final UUID id;
    private final String code;
    private final String description;
    private final BigDecimal discountValue;
    private final OffsetDateTime expirationDate;
    private CouponStatus status;
    private final boolean published;
    private final boolean redeemed;
    private boolean deleted;

    private Coupon(
            UUID id,
            String code,
            String description,
            BigDecimal discountValue,
            OffsetDateTime expirationDate,
            CouponStatus status,
            boolean published,
            boolean redeemed,
            boolean deleted
    ) {
        this.id = id;
        this.code = code;
        this.description = description;
        this.discountValue = discountValue;
        this.expirationDate = expirationDate;
        this.status = status;
        this.published = published;
        this.redeemed = redeemed;
        this.deleted = deleted;
    }

    public static Coupon create(
            String code,
            String description,
            BigDecimal discountValue,
            OffsetDateTime expirationDate,
            Boolean published
    ) {
        String sanitizedCode = sanitizeCode(code);

        validateCode(sanitizedCode);
        validateDescription(description);
        validateDiscountValue(discountValue);
        validateExpirationDate(expirationDate);

        boolean isPublished = Boolean.TRUE.equals(published);
        CouponStatus initialStatus = isPublished ? CouponStatus.ACTIVE : CouponStatus.INACTIVE;

        return new Coupon(
                UUID.randomUUID(),
                sanitizedCode,
                description.trim(),
                discountValue,
                expirationDate,
                initialStatus,
                isPublished,
                false,
                false
        );
    }

    public static Coupon restore(
            UUID id,
            String code,
            String description,
            BigDecimal discountValue,
            OffsetDateTime expirationDate,
            CouponStatus status,
            boolean published,
            boolean redeemed,
            boolean deleted
    ) {
        return new Coupon(
                id,
                code,
                description,
                discountValue,
                expirationDate,
                status,
                published,
                redeemed,
                deleted
        );
    }

    public void delete() {
        if (this.deleted || CouponStatus.DELETED.equals(this.status)) {
            throw new BusinessException("Coupon already deleted");
        }

        this.deleted = true;
        this.status = CouponStatus.DELETED;
    }

    private static String sanitizeCode(String code) {
        if (code == null) {
            return "";
        }

        return code.replaceAll(NON_ALPHANUMERIC_REGEX, "");
    }

    private static void validateCode(String code) {
        if (code.length() != CODE_LENGTH) {
            throw new BusinessException("Coupon code must contain exactly 6 alphanumeric characters");
        }
    }

    private static void validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new BusinessException("Coupon description is required");
        }
    }

    private static void validateDiscountValue(BigDecimal discountValue) {
        if (discountValue == null || discountValue.compareTo(MINIMUM_DISCOUNT_VALUE) < 0) {
            throw new BusinessException("Discount value must be greater than or equal to 0.5");
        }
    }

    private static void validateExpirationDate(OffsetDateTime expirationDate) {
        if (expirationDate == null) {
            throw new BusinessException("Expiration date is required");
        }

        if (expirationDate.isBefore(OffsetDateTime.now())) {
            throw new BusinessException("Expiration date cannot be in the past");
        }
    }
}