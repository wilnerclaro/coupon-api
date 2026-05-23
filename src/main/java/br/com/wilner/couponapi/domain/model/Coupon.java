package br.com.wilner.couponapi.domain.model;

import br.com.wilner.couponapi.domain.enums.CouponStatus;
import br.com.wilner.couponapi.exception.BusinessException;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.UUID;

@Getter
public class Coupon {

    private static final int CODE_LENGTH = 6;
    private static final int DESCRIPTION_MAX_LENGTH = 255;
    private static final int DISCOUNT_SCALE = 2;
    private static final BigDecimal MINIMUM_DISCOUNT_VALUE = new BigDecimal("0.50");
    private static final BigDecimal MAXIMUM_DISCOUNT_VALUE = new BigDecimal("99999999.99");
    private static final String NON_ALPHANUMERIC_REGEX = "[^a-zA-Z0-9]";

    private final UUID id;
    private final String code;
    private final String description;
    private final BigDecimal discountValue;
    private final OffsetDateTime expirationDate;
    private CouponStatus status;
    private boolean published;
    private final boolean redeemed;
    private boolean deleted;
    private final OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private OffsetDateTime deletedAt;

    private Coupon(
            UUID id,
            String code,
            String description,
            BigDecimal discountValue,
            OffsetDateTime expirationDate,
            CouponStatus status,
            boolean published,
            boolean redeemed,
            boolean deleted,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt,
            OffsetDateTime deletedAt
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
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
    }

    public static Coupon create(
            String code,
            String description,
            BigDecimal discountValue,
            OffsetDateTime expirationDate,
            Boolean published
    ) {
        return create(code, description, discountValue, expirationDate, published, Clock.systemUTC());
    }

    public static Coupon create(
            String code,
            String description,
            BigDecimal discountValue,
            OffsetDateTime expirationDate,
            Boolean published,
            Clock clock
    ) {
        String normalizedCode = normalizeCode(code);
        String normalizedDescription = normalizeDescription(description);
        BigDecimal normalizedDiscountValue = normalizeDiscountValue(discountValue);

        validateCode(normalizedCode);
        validateDescription(normalizedDescription);
        validateDiscountValue(normalizedDiscountValue);
        validateExpirationDate(expirationDate, clock);

        boolean isPublished = Boolean.TRUE.equals(published);
        CouponStatus initialStatus = isPublished ? CouponStatus.ACTIVE : CouponStatus.INACTIVE;
        OffsetDateTime now = OffsetDateTime.now(clock);

        return new Coupon(
                UUID.randomUUID(),
                normalizedCode,
                normalizedDescription,
                normalizedDiscountValue,
                expirationDate,
                initialStatus,
                isPublished,
                false,
                false,
                now,
                now,
                null
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
            boolean deleted,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt,
            OffsetDateTime deletedAt
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
                deleted,
                createdAt,
                updatedAt,
                deletedAt
        );
    }

    public void delete() {
        delete(Clock.systemUTC());
    }

    public void delete(Clock clock) {
        if (this.deleted || CouponStatus.DELETED.equals(this.status)) {
            throw new BusinessException("Coupon already deleted");
        }

        OffsetDateTime now = OffsetDateTime.now(clock);

        this.deleted = true;
        this.published = false;
        this.status = CouponStatus.DELETED;
        this.updatedAt = now;
        this.deletedAt = now;
    }

    private static String normalizeCode(String code) {
        if (code == null) {
            return "";
        }

        return code
                .replaceAll(NON_ALPHANUMERIC_REGEX, "")
                .toUpperCase(Locale.ROOT);
    }

    private static String normalizeDescription(String description) {
        if (description == null) {
            return null;
        }

        return description.trim();
    }

    private static BigDecimal normalizeDiscountValue(BigDecimal discountValue) {
        if (discountValue == null) {
            return null;
        }

        return discountValue.setScale(DISCOUNT_SCALE, RoundingMode.UNNECESSARY);
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

        if (description.length() > DESCRIPTION_MAX_LENGTH) {
            throw new BusinessException("Coupon description must contain at most 255 characters");
        }
    }

    private static void validateDiscountValue(BigDecimal discountValue) {
        if (discountValue == null) {
            throw new BusinessException("Discount value is required");
        }

        if (discountValue.compareTo(MINIMUM_DISCOUNT_VALUE) < 0) {
            throw new BusinessException("Discount value must be greater than or equal to 0.5");
        }

        if (discountValue.compareTo(MAXIMUM_DISCOUNT_VALUE) > 0) {
            throw new BusinessException("Discount value must be less than or equal to 99999999.99");
        }
    }

    private static void validateExpirationDate(OffsetDateTime expirationDate, Clock clock) {
        if (expirationDate == null) {
            throw new BusinessException("Expiration date is required");
        }

        if (expirationDate.isBefore(OffsetDateTime.now(clock))) {
            throw new BusinessException("Expiration date cannot be in the past");
        }
    }
}