package com.cg.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.warn("验证失败 - 请求: {} {}", request.getMethod(), request.getRequestURI());
        Map<String, Object> response = new HashMap<>();
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
            log.warn("验证失败 - 字段: {}, 错误: {}", fieldName, errorMessage);
        });

        response.put("success", false);
        response.put("message", "参数验证失败");
        response.put("errors", errors);

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        log.warn("方法不支持 - 请求: {} {}, 方法: {}", request.getMethod(), request.getRequestURI(), ex.getMethod());

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "请求方法不支持: " + ex.getMethod());

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
        log.warn("媒体类型不支持 - 请求: {} {}, 类型: {}", request.getMethod(), request.getRequestURI(), ex.getContentType());

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Content-Type 不支持: " + ex.getContentType());

        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(response);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParameter(MissingServletRequestParameterException ex, HttpServletRequest request) {
        log.warn("缺少请求参数 - 请求: {} {}, 参数: {}", request.getMethod(), request.getRequestURI(), ex.getParameterName());

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "缺少请求参数: " + ex.getParameterName());

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NoHandlerFoundException ex, HttpServletRequest request) {
        log.warn("404错误 - 请求: {} {}", request.getMethod(), request.getRequestURI());

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "请求的资源不存在: " + ex.getRequestURL());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
        log.error("运行时异常 - 请求: {} {}, 错误: {}",
                request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", ex.getMessage());

        if (ex.getMessage() != null && ex.getMessage().contains("用户名")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception ex, HttpServletRequest request) {
        log.error("系统异常 - 请求: {} {}, 错误: {}",
                request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "系统错误: " + ex.getClass().getSimpleName());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
