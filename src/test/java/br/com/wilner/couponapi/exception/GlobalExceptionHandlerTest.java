package br.com.wilner.couponapi.exception;

import br.com.wilner.couponapi.dto.response.ErrorResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final MockHttpServletRequest request = new MockHttpServletRequest("POST", "/coupon");

    @Test
    void handleBusinessExceptionShouldReturnBadRequest() {
        ResponseEntity<ErrorResponse> response = handler.handleBusinessException(
                new BusinessException("Invalid business rule"),
                request
        );

        assertError(response, HttpStatus.BAD_REQUEST, "Invalid business rule");
    }

    @Test
    void handleDuplicateResourceExceptionShouldReturnConflict() {
        ResponseEntity<ErrorResponse> response = handler.handleDuplicateResourceException(
                new DuplicateResourceException("Coupon code already exists"),
                request
        );

        assertError(response, HttpStatus.CONFLICT, "Coupon code already exists");
    }

    @Test
    void handleResourceNotFoundExceptionShouldReturnNotFound() {
        ResponseEntity<ErrorResponse> response = handler.handleResourceNotFoundException(
                new ResourceNotFoundException("Coupon not found"),
                request
        );

        assertError(response, HttpStatus.NOT_FOUND, "Coupon not found");
    }

    @Test
    void handleMethodArgumentNotValidExceptionShouldReturnValidationDetails() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "code", "Coupon code is required"));
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(
                mock(MethodParameter.class),
                bindingResult
        );

        ResponseEntity<ErrorResponse> response = handler.handleMethodArgumentNotValidException(exception, request);

        assertError(response, HttpStatus.BAD_REQUEST, "Invalid request data");
        assertThat(response.getBody().details()).containsExactly("code: Coupon code is required");
    }

    @Test
    void handleHttpMessageNotReadableExceptionShouldReturnMalformedRequestMessage() {
        ResponseEntity<ErrorResponse> response = handler.handleHttpMessageNotReadableException(
                new HttpMessageNotReadableException("Invalid JSON", mock(HttpInputMessage.class)),
                request
        );

        assertError(response, HttpStatus.BAD_REQUEST, "Malformed JSON request or invalid field type");
    }

    @Test
    void handleConstraintViolationExceptionShouldReturnValidationDetails() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<ValidationFixture>> violations = validator.validate(new ValidationFixture(""));

        ResponseEntity<ErrorResponse> response = handler.handleConstraintViolationException(
                new jakarta.validation.ConstraintViolationException(violations),
                request
        );

        assertError(response, HttpStatus.BAD_REQUEST, "Invalid request data");
        assertThat(response.getBody().details())
                .singleElement()
                .asString()
                .startsWith("name: ");
    }

    @Test
    void handleDataIntegrityViolationExceptionShouldReturnConflict() {
        ResponseEntity<ErrorResponse> response = handler.handleDataIntegrityViolationException(
                new DataIntegrityViolationException("duplicate"),
                request
        );

        assertError(response, HttpStatus.CONFLICT, "Request violates database constraints");
    }

    @Test
    void handleUnexpectedExceptionShouldReturnInternalServerError() {
        ResponseEntity<ErrorResponse> response = handler.handleUnexpectedException(
                new RuntimeException("boom"),
                request
        );

        assertError(response, HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    private static void assertError(
            ResponseEntity<ErrorResponse> response,
            HttpStatus status,
            String message
    ) {
        assertThat(response.getStatusCode()).isEqualTo(status);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().timestamp()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(status.value());
        assertThat(response.getBody().error()).isEqualTo(status.getReasonPhrase());
        assertThat(response.getBody().message()).isEqualTo(message);
        assertThat(response.getBody().path()).isEqualTo("/coupon");
    }

    private record ValidationFixture(@NotBlank String name) {
    }
}
