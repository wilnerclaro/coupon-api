package br.com.wilner.couponapi.mapper;

import br.com.wilner.couponapi.domain.model.Coupon;
import br.com.wilner.couponapi.dto.response.CouponResponse;
import br.com.wilner.couponapi.persistence.entity.CouponEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface CouponMapper {

    CouponEntity toEntity(Coupon coupon);

    CouponResponse toResponse(Coupon coupon);

    void updateEntityFromDomain(Coupon coupon, @MappingTarget CouponEntity entity);

    default Coupon toDomain(CouponEntity entity) {
        if (entity == null) {
            return null;
        }

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
}