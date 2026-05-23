package br.com.wilner.couponapi.service;

import br.com.wilner.couponapi.domain.model.Coupon;
import br.com.wilner.couponapi.dto.request.CouponCreateRequest;
import br.com.wilner.couponapi.dto.response.CouponResponse;
import br.com.wilner.couponapi.exception.ResourceNotFoundException;
import br.com.wilner.couponapi.mapper.CouponMapper;
import br.com.wilner.couponapi.persistence.entity.CouponEntity;
import br.com.wilner.couponapi.persistence.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponMapper couponMapper;
    private final CouponRepository couponRepository;

    @Transactional
    public CouponResponse create (CouponCreateRequest request){
        Coupon coupon = Coupon.create(
                request.code(),
                request.description(),
                request.discountValue(),
                request.expirationDate(),
                request.published()
        );
        CouponEntity entity = couponMapper.toEntity(coupon);
        CouponEntity saveEntity = couponRepository.save(entity);
        Coupon savedCoupon = couponMapper.toDomain(saveEntity);

        return couponMapper.toResponse(savedCoupon);

    }

    @Transactional(readOnly = true)
    public CouponResponse findById(UUID id){
        CouponEntity entity = findActiveEntityById(id);
        Coupon coupon = couponMapper.toDomain(entity);

        return couponMapper.toResponse(coupon);
    }

    @Transactional
    public void  delete(UUID id){
        CouponEntity entity = findActiveEntityById(id);
        Coupon coupon = couponMapper.toDomain(entity);

        coupon.delete();

        couponMapper.updateEntityFromDomain(coupon,entity);
        couponRepository.save(entity);
    }

    private CouponEntity findActiveEntityById(UUID id) {
        return couponRepository.findByIdAndDeletedFalse(id).orElseThrow(
                () -> new ResourceNotFoundException("Coupon not found"));
    }
}
