package com.automobile.ecom.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /* ---------- NOT FOUND ---------- */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request){

        return build(ex.getMessage(), ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethodNotSupported(
            Exception ex,
            HttpServletRequest request) {

        return build(
                "HTTP method not supported for this endpoint",
                ErrorCode.BAD_REQUEST,
                HttpStatus.METHOD_NOT_ALLOWED,
                request
        );
    }

    @ExceptionHandler(org.springframework.web.servlet.NoHandlerFoundException.class)
    public ResponseEntity<ApiError> handleNoHandlerFound(
            Exception ex,
            HttpServletRequest request) {

        return build(
                "API endpoint not found",
                ErrorCode.NOT_FOUND,
                HttpStatus.NOT_FOUND,
                request
        );
    }

    /* ---------- BAD REQUEST ---------- */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequest(
            BadRequestException ex,
            HttpServletRequest request){

        return build(ex.getMessage(), ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, request);
    }

    /* ---------- VALIDATION ---------- */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request){

        String msg = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");

        return build(msg, ErrorCode.VALIDATION_FAILED, HttpStatus.BAD_REQUEST, request);
    }

    /* ---------- UNAUTHORIZED ---------- */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiError> handleUnauthorized(
            UnauthorizedException ex,
            HttpServletRequest request){

        return build(ex.getMessage(), ErrorCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED, request);
    }

    /* ---------- FORBIDDEN ---------- */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleForbidden(
            AccessDeniedException ex,
            HttpServletRequest request){

        return build("Access Denied", ErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN, request);
    }

    /* ---------- FALLBACK (IMPORTANT FIX) ---------- */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAll(
            Exception ex,
            HttpServletRequest request){

        log.error("Unhandled Exception: ", ex);

        return build(
                "Something went wrong",
                ErrorCode.SERVER_ERROR,
                HttpStatus.INTERNAL_SERVER_ERROR,
                request
        );
    }

    /* ---------- BUILDER ---------- */
    private ResponseEntity<ApiError> build(
            String message,
            ErrorCode code,
            HttpStatus status,
            HttpServletRequest request){

        ApiError error = ApiError.builder()
                .message(message)
                .code(code.name())
                .status(status.value())
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return new ResponseEntity<>(error, status);
    }
}