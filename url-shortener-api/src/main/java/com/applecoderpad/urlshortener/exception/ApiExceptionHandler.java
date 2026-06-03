package com.applecoderpad.urlshortener.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
  @ExceptionHandler(NotFoundException.class)
  public ProblemDetail notFound(NotFoundException ex, HttpServletRequest request) {
    return problem(HttpStatus.NOT_FOUND, ex.getMessage(), request);
  }

  @ExceptionHandler(ConflictException.class)
  public ProblemDetail conflict(ConflictException ex, HttpServletRequest request) {
    return problem(HttpStatus.CONFLICT, ex.getMessage(), request);
  }

  @ExceptionHandler(BadRequestException.class)
  public ProblemDetail badRequest(BadRequestException ex, HttpServletRequest request) {
    return problem(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
  }

  @ExceptionHandler(GoneException.class)
  public ProblemDetail gone(GoneException ex, HttpServletRequest request) {
    return problem(HttpStatus.GONE, ex.getMessage(), request);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail validation(MethodArgumentNotValidException ex, HttpServletRequest request) {
    ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "request validation failed", request);
    problem.setProperty("errors", fieldErrors(ex));
    return problem;
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ProblemDetail constraint(ConstraintViolationException ex, HttpServletRequest request) {
    return problem(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
  }

  private static ProblemDetail problem(
      HttpStatus status, String detail, HttpServletRequest request) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
    problem.setTitle(status.getReasonPhrase());
    problem.setInstance(URI.create(request.getRequestURI()));
    problem.setProperty("code", status.name());
    return problem;
  }

  private static Map<String, String> fieldErrors(MethodArgumentNotValidException ex) {
    Map<String, String> errors = new LinkedHashMap<>();
    ex.getBindingResult()
        .getFieldErrors()
        .forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
    return errors;
  }
}
