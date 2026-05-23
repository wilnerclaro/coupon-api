package br.com.wilner.couponapi.service;

import br.com.wilner.couponapi.domain.model.Coupon;
import br.com.wilner.couponapi.dto.request.CouponCreateRequest;
import br.com.wilner.couponapi.dto.response.CouponResponse;
import br.com.wilner.couponapi.exception.DuplicateResourceException;
import br.com.wilner.couponapi.exception.ResourceNotFoundException;
import br.com.wilner.couponapi.mapper.CouponMapper;
import br.com.wilner.couponapi.persistence.entity.CouponEntity;
import br.com.wilner.couponapi.persistence.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponMapper couponMapper;

    @Transactional
    public CouponResponse create(CouponCreateRequest request) {
        Coupon coupon = Coupon.create(
                request.code(),
                request.description(),
                request.discountValue(),
                request.expirationDate(),
                request.published()
        );

        validateCodeAvailability(coupon.getCode());

        CouponEntity entity = couponMapper.toEntity(coupon);

        try {
            CouponEntity savedEntity = couponRepository.save(entity);
            Coupon savedCoupon = couponMapper.toDomain(savedEntity);

            log.info("Coupon created successfully. id={}, code={}", savedCoupon.getId(), savedCoupon.getCode());

            return couponMapper.toResponse(savedCoupon);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateResourceException("Coupon code already exists", exception);
        }
    }

    @Transactional(readOnly = true)
    public CouponResponse findById(UUID id) {
        CouponEntity entity = findActiveEntityById(id);
        Coupon coupon = couponMapper.toDomain(entity);

        return couponMapper.toResponse(coupon);
    }

    @Transactional
    public void delete(UUID id) {
        CouponEntity entity = findActiveEntityById(id);
        Coupon coupon = couponMapper.toDomain(entity);

        coupon.delete();

        couponMapper.updateEntityFromDomain(coupon, entity);

        log.info("Coupon deleted successfully. id={}, code={}", coupon.getId(), coupon.getCode());
    }

    private void validateCodeAvailability(String code) {
        if (couponRepository.existsByCodeAndDeletedFalse(code)) {
            throw new DuplicateResourceException("Coupon code already exists");
        }
    }

    private CouponEntity findActiveEntityById(UUID id) {
        return couponRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));
    }
}