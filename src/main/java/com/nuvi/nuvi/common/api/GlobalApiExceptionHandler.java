package com.nuvi.nuvi.common.api;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

@RestControllerAdvice
public class GlobalApiExceptionHandler {

    private final RequestMetaFactory metaFactory;

    public GlobalApiExceptionHandler(RequestMetaFactory metaFactory) {
        this.metaFactory = metaFactory;
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException exception) {
        return error(exception.status(), exception.code(), exception.getMessage(), exception.details());
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MissingRequestHeaderException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception exception) {
        return error(HttpStatus.BAD_REQUEST, ApiErrorCode.VALIDATION_FAILED, ApiErrorCode.VALIDATION_FAILED.defaultMessage());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException exception) {
        return error(HttpStatus.METHOD_NOT_ALLOWED, ApiErrorCode.VALIDATION_FAILED, "HTTP method is not supported for this resource.");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException exception) {
        return error(HttpStatus.NOT_FOUND, ApiErrorCode.CART_NOT_FOUND, "Resource was not found.");
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException exception) {
        HttpStatus status = HttpStatus.resolve(exception.getStatusCode().value());
        HttpStatus resolvedStatus = status == null ? HttpStatus.INTERNAL_SERVER_ERROR : status;
        ApiErrorCode code = codeForStatus(resolvedStatus);
        String message = exception.getReason() == null ? code.defaultMessage() : exception.getReason();
        return error(resolvedStatus, code, message);
    }

    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<ErrorResponse> handleErrorResponse(ErrorResponseException exception) {
        HttpStatus status = HttpStatus.resolve(exception.getStatusCode().value());
        HttpStatus resolvedStatus = status == null ? HttpStatus.INTERNAL_SERVER_ERROR : status;
        ApiErrorCode code = codeForStatus(resolvedStatus);
        return error(resolvedStatus, code, code.defaultMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception exception, HttpServletRequest request) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, ApiErrorCode.INTERNAL_ERROR, ApiErrorCode.INTERNAL_ERROR.defaultMessage());
    }

    private ResponseEntity<ErrorResponse> error(HttpStatus status, ApiErrorCode code, String message) {
        return error(status, code, message, List.of());
    }

    private ResponseEntity<ErrorResponse> error(HttpStatus status, ApiErrorCode code, String message, List<ErrorDetail> details) {
        ApiError error = ApiError.of(code, message, details);
        return ResponseEntity.status(status).body(ErrorResponse.of(error, metaFactory.current()));
    }

    private ApiErrorCode codeForStatus(HttpStatus status) {
        return switch (status) {
            case UNAUTHORIZED -> ApiErrorCode.AUTH_REQUIRED;
            case FORBIDDEN -> ApiErrorCode.FORBIDDEN;
            case TOO_MANY_REQUESTS -> ApiErrorCode.RATE_LIMITED;
            case NOT_FOUND -> ApiErrorCode.CART_NOT_FOUND;
            case CONFLICT -> ApiErrorCode.IDEMPOTENCY_KEY_CONFLICT;
            default -> status.is4xxClientError() ? ApiErrorCode.VALIDATION_FAILED : ApiErrorCode.INTERNAL_ERROR;
        };
    }
}
