package br.com.wilner.couponapi.persistence.repository;

import br.com.wilner.couponapi.persistence.entity.CouponEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CouponRepository extends JpaRepository<CouponEntity, UUID> {

    Optional<CouponEntity> findByIdAndDeletedFalse(UUID id);

    boolean existsByCodeAndDeletedFalse(String code);
}