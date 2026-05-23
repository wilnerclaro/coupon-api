package br.com.wilner.couponapi.domain.model;

import br.com.wilner.couponapi.domain.enums.CouponStatus;
import br.com.wilner.couponapi.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CouponTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-05-23T12:00:00Z"), ZoneOffset.UTC);
    private static final OffsetDateTime FUTURE_DATE = OffsetDateTime.now(CLOCK).plusDays(1);

    @Test
    void createShouldNormalizeFieldsAndCreateInactiveCouponWhenNotPublished() {
        Coupon coupon = Coupon.create(
                "ab-c 12!3",
                "  Black Friday coupon  ",
                new BigDecimal("10.50"),
                FUTURE_DATE,
                null,
                CLOCK
        );

        assertThat(coupon.getId()).isNotNull();
        assertThat(coupon.getCode()).isEqualTo("ABC123");
        assertThat(coupon.getDescription()).isEqualTo("Black Friday coupon");
        assertThat(coupon.getDiscountValue()).isEqualByComparingTo("10.50");
        assertThat(coupon.getExpirationDate()).isEqualTo(FUTURE_DATE);
        assertThat(coupon.getStatus()).isEqualTo(CouponStatus.INACTIVE);
        assertThat(coupon.isPublished()).isFalse();
        assertThat(coupon.isRedeemed()).isFalse();
        assertThat(coupon.isDeleted()).isFalse();
        assertThat(coupon.getCreatedAt()).isEqualTo(OffsetDateTime.now(CLOCK));
        assertThat(coupon.getUpdatedAt()).isEqualTo(OffsetDateTime.now(CLOCK));
        assertThat(coupon.getDeletedAt()).isNull();
    }

    @Test
    void createShouldCreateActiveCouponWhenPublished() {
        Coupon coupon = Coupon.create("abc123", "Published coupon", new BigDecimal("1.00"), FUTURE_DATE, true, CLOCK);

        assertThat(coupon.getStatus()).isEqualTo(CouponStatus.ACTIVE);
        assertThat(coupon.isPublished()).isTrue();
    }

    @Test
    void createShouldRejectInvalidCode() {
        assertThatThrownBy(() -> Coupon.create(null, "Coupon", new BigDecimal("1.00"), FUTURE_DATE, false, CLOCK))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Coupon code must contain exactly 6 alphanumeric characters");
    }

    @Test
    void createShouldRejectBlankDescription() {
        assertThatThrownBy(() -> Coupon.create("ABC123", "  ", new BigDecimal("1.00"), FUTURE_DATE, false, CLOCK))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Coupon description is required");
    }

    @Test
    void createShouldRejectDescriptionLongerThan255Characters() {
        String description = "a".repeat(256);

        assertThatThrownBy(() -> Coupon.create("ABC123", description, new BigDecimal("1.00"), FUTURE_DATE, false, CLOCK))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Coupon description must contain at most 255 characters");
    }

    @Test
    void createShouldRejectNullDiscountValue() {
        assertThatThrownBy(() -> Coupon.create("ABC123", "Coupon", null, FUTURE_DATE, false, CLOCK))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Discount value is required");
    }

    @Test
    void createShouldRejectDiscountValueWithMoreThanTwoDecimalPlaces() {
        assertThatThrownBy(() -> Coupon.create("ABC123", "Coupon", new BigDecimal("1.001"), FUTURE_DATE, false, CLOCK))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Discount value must have at most 2 decimal places");
    }

    @Test
    void createShouldRejectDiscountValueBelowMinimum() {
        assertThatThrownBy(() -> Coupon.create("ABC123", "Coupon", new BigDecimal("0.49"), FUTURE_DATE, false, CLOCK))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Discount value must be greater than or equal to 0.5");
    }

    @Test
    void createShouldRejectDiscountValueAboveMaximum() {
        assertThatThrownBy(() -> Coupon.create("ABC123", "Coupon", new BigDecimal("100000000.00"), FUTURE_DATE, false, CLOCK))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Discount value must be less than or equal to 99999999.99");
    }

    @Test
    void createShouldRejectNullExpirationDate() {
        assertThatThrownBy(() -> Coupon.create("ABC123", "Coupon", new BigDecimal("1.00"), null, false, CLOCK))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Expiration date is required");
    }

    @Test
    void createShouldRejectExpirationDateInThePast() {
        OffsetDateTime pastDate = OffsetDateTime.now(CLOCK).minusSeconds(1);

        assertThatThrownBy(() -> Coupon.create("ABC123", "Coupon", new BigDecimal("1.00"), pastDate, false, CLOCK))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Expiration date cannot be in the past");
    }

    @Test
    void deleteShouldMarkCouponAsDeleted() {
        Coupon coupon = Coupon.create("ABC123", "Coupon", new BigDecimal("1.00"), FUTURE_DATE, true, CLOCK);
        Clock deletionClock = Clock.fixed(Instant.parse("2026-05-24T15:30:00Z"), ZoneOffset.UTC);

        coupon.delete(deletionClock);

        assertThat(coupon.isDeleted()).isTrue();
        assertThat(coupon.isPublished()).isFalse();
        assertThat(coupon.getStatus()).isEqualTo(CouponStatus.DELETED);
        assertThat(coupon.getUpdatedAt()).isEqualTo(OffsetDateTime.now(deletionClock));
        assertThat(coupon.getDeletedAt()).isEqualTo(OffsetDateTime.now(deletionClock));
    }

    @Test
    void deleteShouldRejectAlreadyDeletedCoupon() {
        Coupon coupon = Coupon.create("ABC123", "Coupon", new BigDecimal("1.00"), FUTURE_DATE, true, CLOCK);
        coupon.delete(CLOCK);

        assertThatThrownBy(() -> coupon.delete(CLOCK))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Coupon already deleted");
    }
}
