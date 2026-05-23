package br.com.wilner.couponapi.mapper;

import br.com.wilner.couponapi.domain.enums.CouponStatus;
import br.com.wilner.couponapi.domain.model.Coupon;
import br.com.wilner.couponapi.dto.response.CouponResponse;
import br.com.wilner.couponapi.persistence.entity.CouponEntity;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CouponMapperTest {

    private final CouponMapper mapper = Mappers.getMapper(CouponMapper.class);

    @Test
    void toEntityShouldMapDomainCoupon() {
        Coupon coupon = coupon();

        CouponEntity entity = mapper.toEntity(coupon);

        assertThat(entity.getId()).isEqualTo(coupon.getId());
        assertThat(entity.getCode()).isEqualTo(coupon.getCode());
        assertThat(entity.getDescription()).isEqualTo(coupon.getDescription());
        assertThat(entity.getDiscountValue()).isEqualByComparingTo(coupon.getDiscountValue());
        assertThat(entity.getExpirationDate()).isEqualTo(coupon.getExpirationDate());
        assertThat(entity.getStatus()).isEqualTo(coupon.getStatus());
        assertThat(entity.isPublished()).isEqualTo(coupon.isPublished());
        assertThat(entity.isRedeemed()).isEqualTo(coupon.isRedeemed());
        assertThat(entity.isDeleted()).isEqualTo(coupon.isDeleted());
        assertThat(entity.getCreatedAt()).isEqualTo(coupon.getCreatedAt());
        assertThat(entity.getUpdatedAt()).isEqualTo(coupon.getUpdatedAt());
        assertThat(entity.getDeletedAt()).isEqualTo(coupon.getDeletedAt());
    }

    @Test
    void toResponseShouldMapDomainCoupon() {
        Coupon coupon = coupon();

        CouponResponse response = mapper.toResponse(coupon);

        assertThat(response.id()).isEqualTo(coupon.getId());
        assertThat(response.code()).isEqualTo(coupon.getCode());
        assertThat(response.description()).isEqualTo(coupon.getDescription());
        assertThat(response.discountValue()).isEqualByComparingTo(coupon.getDiscountValue());
        assertThat(response.expirationDate()).isEqualTo(coupon.getExpirationDate());
        assertThat(response.status()).isEqualTo(coupon.getStatus());
        assertThat(response.published()).isEqualTo(coupon.isPublished());
        assertThat(response.redeemed()).isEqualTo(coupon.isRedeemed());
    }

    @Test
    void updateEntityFromDomainShouldCopyMutableState() {
        Coupon coupon = coupon();
        CouponEntity entity = new CouponEntity();

        mapper.updateEntityFromDomain(coupon, entity);

        assertThat(entity.getId()).isEqualTo(coupon.getId());
        assertThat(entity.getCode()).isEqualTo(coupon.getCode());
        assertThat(entity.getStatus()).isEqualTo(coupon.getStatus());
    }

    @Test
    void toDomainShouldRestoreEntityAndReturnNullForNullEntity() {
        CouponEntity entity = CouponEntity.builder()
                .id(UUID.randomUUID())
                .code("ABC123")
                .description("Coupon")
                .discountValue(new BigDecimal("2.00"))
                .expirationDate(OffsetDateTime.now().plusDays(3))
                .status(CouponStatus.ACTIVE)
                .published(true)
                .redeemed(false)
                .deleted(false)
                .createdAt(OffsetDateTime.now().minusDays(1))
                .updatedAt(OffsetDateTime.now())
                .build();

        Coupon coupon = mapper.toDomain(entity);

        assertThat(coupon.getId()).isEqualTo(entity.getId());
        assertThat(coupon.getCode()).isEqualTo(entity.getCode());
        assertThat(coupon.getStatus()).isEqualTo(entity.getStatus());
        assertThat(mapper.toDomain(null)).isNull();
    }

    private static Coupon coupon() {
        OffsetDateTime now = OffsetDateTime.now();

        return Coupon.restore(
                UUID.randomUUID(),
                "ABC123",
                "Coupon",
                new BigDecimal("2.00"),
                now.plusDays(3),
                CouponStatus.ACTIVE,
                true,
                false,
                false,
                now.minusDays(1),
                now,
                null
        );
    }
}
