package br.com.wilner.couponapi.service;

import br.com.wilner.couponapi.domain.enums.CouponStatus;
import br.com.wilner.couponapi.domain.model.Coupon;
import br.com.wilner.couponapi.dto.request.CouponCreateRequest;
import br.com.wilner.couponapi.dto.response.CouponResponse;
import br.com.wilner.couponapi.exception.DuplicateResourceException;
import br.com.wilner.couponapi.exception.ResourceNotFoundException;
import br.com.wilner.couponapi.mapper.CouponMapper;
import br.com.wilner.couponapi.persistence.entity.CouponEntity;
import br.com.wilner.couponapi.persistence.repository.CouponRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponMapper couponMapper;

    @InjectMocks
    private CouponService couponService;

    @Test
    void createShouldSaveCouponAndReturnResponse() {
        OffsetDateTime expirationDate = OffsetDateTime.now().plusDays(10);
        CouponCreateRequest request = new CouponCreateRequest(
                "abc-123",
                "Coupon",
                new BigDecimal("2.00"),
                expirationDate,
                true
        );
        CouponEntity entity = new CouponEntity();
        CouponEntity savedEntity = entity("ABC123", expirationDate);
        Coupon savedCoupon = domain(savedEntity);
        CouponResponse expectedResponse = response(savedCoupon);

        when(couponRepository.existsByCodeAndDeletedFalse("ABC123")).thenReturn(false);
        when(couponMapper.toEntity(any(Coupon.class))).thenReturn(entity);
        when(couponRepository.save(entity)).thenReturn(savedEntity);
        when(couponMapper.toDomain(savedEntity)).thenReturn(savedCoupon);
        when(couponMapper.toResponse(savedCoupon)).thenReturn(expectedResponse);

        CouponResponse response = couponService.create(request);

        assertThat(response).isEqualTo(expectedResponse);
        ArgumentCaptor<Coupon> couponCaptor = ArgumentCaptor.forClass(Coupon.class);
        verify(couponMapper).toEntity(couponCaptor.capture());
        assertThat(couponCaptor.getValue().getCode()).isEqualTo("ABC123");
        verify(couponRepository).save(entity);
    }

    @Test
    void createShouldRejectExistingCodeBeforeSaving() {
        CouponCreateRequest request = new CouponCreateRequest(
                "abc-123",
                "Coupon",
                new BigDecimal("2.00"),
                OffsetDateTime.now().plusDays(10),
                true
        );

        when(couponRepository.existsByCodeAndDeletedFalse("ABC123")).thenReturn(true);

        assertThatThrownBy(() -> couponService.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Coupon code already exists");

        verify(couponRepository, never()).save(any());
    }

    @Test
    void createShouldTranslateDatabaseConstraintViolationToDuplicateResourceException() {
        CouponCreateRequest request = new CouponCreateRequest(
                "abc-123",
                "Coupon",
                new BigDecimal("2.00"),
                OffsetDateTime.now().plusDays(10),
                true
        );
        CouponEntity entity = new CouponEntity();

        when(couponRepository.existsByCodeAndDeletedFalse("ABC123")).thenReturn(false);
        when(couponMapper.toEntity(any(Coupon.class))).thenReturn(entity);
        when(couponRepository.save(entity)).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> couponService.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Coupon code already exists")
                .hasCauseInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void findByIdShouldReturnActiveCoupon() {
        UUID id = UUID.randomUUID();
        CouponEntity entity = entity("ABC123", OffsetDateTime.now().plusDays(5));
        Coupon coupon = domain(entity);
        CouponResponse expectedResponse = response(coupon);

        when(couponRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));
        when(couponMapper.toDomain(entity)).thenReturn(coupon);
        when(couponMapper.toResponse(coupon)).thenReturn(expectedResponse);

        CouponResponse response = couponService.findById(id);

        assertThat(response).isEqualTo(expectedResponse);
    }

    @Test
    void findByIdShouldThrowWhenCouponDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(couponRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> couponService.findById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Coupon not found");
    }

    @Test
    void deleteShouldUpdateEntityFromDeletedDomainCoupon() {
        UUID id = UUID.randomUUID();
        CouponEntity entity = entity("ABC123", OffsetDateTime.now().plusDays(5));
        Coupon coupon = domain(entity);

        when(couponRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(entity));
        when(couponMapper.toDomain(entity)).thenReturn(coupon);

        couponService.delete(id);

        ArgumentCaptor<Coupon> couponCaptor = ArgumentCaptor.forClass(Coupon.class);
        verify(couponMapper).updateEntityFromDomain(couponCaptor.capture(), org.mockito.Mockito.eq(entity));
        assertThat(couponCaptor.getValue().isDeleted()).isTrue();
        assertThat(couponCaptor.getValue().getStatus()).isEqualTo(CouponStatus.DELETED);
    }

    private static CouponEntity entity(String code, OffsetDateTime expirationDate) {
        return CouponEntity.builder()
                .id(UUID.randomUUID())
                .code(code)
                .description("Coupon")
                .discountValue(new BigDecimal("2.00"))
                .expirationDate(expirationDate)
                .status(CouponStatus.ACTIVE)
                .published(true)
                .redeemed(false)
                .deleted(false)
                .createdAt(OffsetDateTime.now().minusDays(1))
                .updatedAt(OffsetDateTime.now().minusDays(1))
                .build();
    }

    private static Coupon domain(CouponEntity entity) {
        return Coupon.restore(
                entity.getId(),
                entity.getCode(),
                entity.getDescription(),
                entity.getDiscountValue(),
                entity.getExpirationDate(),
                entity.getStatus(),
                entity.isPublished(),
                entity.isRedeemed(),
                entity.isDeleted(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    private static CouponResponse response(Coupon coupon) {
        return new CouponResponse(
                coupon.getId(),
                coupon.getCode(),
                coupon.getDescription(),
                coupon.getDiscountValue(),
                coupon.getExpirationDate(),
                coupon.getStatus(),
                coupon.isPublished(),
                coupon.isRedeemed()
        );
    }
}
