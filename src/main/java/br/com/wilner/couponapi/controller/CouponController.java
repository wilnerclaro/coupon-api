package br.com.wilner.couponapi.controller;

import br.com.wilner.couponapi.dto.request.CouponCreateRequest;
import br.com.wilner.couponapi.dto.response.CouponResponse;
import br.com.wilner.couponapi.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/coupon")
@RequiredArgsConstructor
@Tag(name = "Coupons", description = "Operations for coupon management")
public class CouponController {

    private final CouponService service;

    @PostMapping
    @Operation(summary = "Create a new coupon")
    public ResponseEntity<CouponResponse>create(
            @Valid @RequestBody CouponCreateRequest request){

        CouponResponse response = service.create(request);
        URI location = URI.create("/coupon/%s".formatted(response.id()));
        return ResponseEntity.created(location).body(response);

    }

    @GetMapping("/{id}")
    @Operation(summary = "Find coupon by id")
    public ResponseEntity<CouponResponse>findById(@PathVariable UUID id){
        CouponResponse response = service.findById(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete coupon by id")
    public ResponseEntity<Void>delete(@PathVariable UUID id){
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
