package br.com.wilner.couponapi.controller;

import br.com.wilner.couponapi.domain.enums.CouponStatus;
import br.com.wilner.couponapi.dto.request.CouponCreateRequest;
import br.com.wilner.couponapi.dto.response.CouponResponse;
import br.com.wilner.couponapi.service.CouponService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CouponControllerTest {

    private final CouponService couponService = mock(CouponService.class);
    private final CouponController couponController = new CouponController(couponService);

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void createShouldReturnCreatedResponseWithLocationHeader() {
        UUID id = UUID.randomUUID();
        OffsetDateTime expirationDate = OffsetDateTime.now().plusDays(10);
        CouponCreateRequest request = new CouponCreateRequest(
                "ABC123",
                "Coupon",
                new BigDecimal("2.00"),
                expirationDate,
                true
        );
        CouponResponse serviceResponse = new CouponResponse(
                id,
                "ABC123",
                "Coupon",
                new BigDecimal("2.00"),
                expirationDate,
                CouponStatus.ACTIVE,
                true,
                false
        );
        MockHttpServletRequest servletRequest = new MockHttpServletRequest("POST", "/coupon");
        servletRequest.setScheme("http");
        servletRequest.setServerName("localhost");
        servletRequest.setServerPort(8080);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(servletRequest));

        when(couponService.create(request)).thenReturn(serviceResponse);

        ResponseEntity<CouponResponse> response = couponController.create(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(serviceResponse);
        assertThat(response.getHeaders().getLocation()).isEqualTo(URI.create("http://localhost:8080/coupon/" + id));
    }

    @Test
    void findByIdShouldReturnOkResponse() {
        UUID id = UUID.randomUUID();
        CouponResponse serviceResponse = new CouponResponse(
                id,
                "ABC123",
                "Coupon",
                new BigDecimal("2.00"),
                OffsetDateTime.now().plusDays(10),
                CouponStatus.ACTIVE,
                true,
                false
        );

        when(couponService.findById(id)).thenReturn(serviceResponse);

        ResponseEntity<CouponResponse> response = couponController.findById(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(serviceResponse);
    }

    @Test
    void deleteShouldReturnNoContentResponse() {
        UUID id = UUID.randomUUID();

        ResponseEntity<Void> response = couponController.delete(id);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        verify(couponService).delete(id);
    }
}
