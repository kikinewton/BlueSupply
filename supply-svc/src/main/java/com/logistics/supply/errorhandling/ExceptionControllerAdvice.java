package com.logistics.supply.errorhandling;

import com.logistics.supply.exception.BadRequestException;
import com.logistics.supply.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class ExceptionControllerAdvice {

  @ExceptionHandler(value = NotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public ResponseEntity<ApiError> handleNotFoundException(NotFoundException notFoundException) {

    String message = notFoundException.getMessage();
    log.error(message, notFoundException);
    ApiError apiError = new ApiError(HttpStatus.NOT_FOUND, message);
    return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(value = BadRequestException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ApiError> handleBadException(BadRequestException badRequestException) {

    String message = badRequestException.getMessage();
    log.error(message, badRequestException);
    ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, message);
    return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(
      value = {
        IllegalArgumentException.class,
        MethodArgumentTypeMismatchException.class,
        HttpMessageNotReadableException.class
      })
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ApiError> handleGeneralBadRequestException(Exception exception) {

    String message = exception.getMessage();
    log.error(message, exception);
    ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, exception.getLocalizedMessage());
    return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ApiError> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
    BindingResult bindingResult = ex.getBindingResult();
    // Iterate over field errors to retrieve error messages
    final List<String> errors = new ArrayList<>();
    for (FieldError fieldError : bindingResult.getFieldErrors()) {
      String defaultMessage = fieldError.getDefaultMessage();
      errors.add(defaultMessage);
    }
    String errorStr = String.join(", ", errors);
    ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, errorStr);
    return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
}

    @ExceptionHandler(value = {RuntimeException.class, UnsupportedOperationException.class})
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ResponseEntity<ApiError> handleRuntimeException(RuntimeException exception) {

    String message = exception.getMessage();
    log.error(message, exception);
    ApiError apiError = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, message);
    return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler({BadCredentialsException.class})
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  protected ResponseEntity<?> handleBadCredentials(
      final BadCredentialsException badCredentialsException) {

    log.error(badCredentialsException.getMessage(), badCredentialsException);
    final ApiError apiError = new ApiError(HttpStatus.UNAUTHORIZED, "INVALID CREDENTIALS");
    return new ResponseEntity<>(apiError, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler({org.springframework.security.access.AccessDeniedException.class})
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public ResponseEntity<?> handleAccessDenied(
      final org.springframework.security.access.AccessDeniedException accessDeniedException) {

    log.info(accessDeniedException.getMessage(), accessDeniedException);
    final ApiError apiError = new ApiError(HttpStatus.FORBIDDEN, "ACCESS DENIED");
    return new ResponseEntity<>(apiError, HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<Object> handleConstraintViolation(
      final ConstraintViolationException constraintViolationException) {

    log.error(constraintViolationException.getMessage(), constraintViolationException);

    final List<String> errors = new ArrayList<>();
    for (final ConstraintViolation<?> violation : constraintViolationException.getConstraintViolations()) {
      errors.add(
          violation.getRootBeanClass().getName()
              + " "
              + violation.getPropertyPath()
              + ": "
              + violation.getMessage());
    }

    String errorStr = String.join("", errors);
    final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, errorStr);
    return new ResponseEntity<>(apiError,  HttpStatus.BAD_REQUEST);
  }


  @ExceptionHandler({DataIntegrityViolationException.class})
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<Object> handleDataIntegrityViolationConflict(
          final DataIntegrityViolationException dataIntegrityViolationException) {

    log.error(dataIntegrityViolationException.getMessage(), dataIntegrityViolationException);

    final String error = dataIntegrityViolationException.getMostSpecificCause().getLocalizedMessage();
    final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, error);
    return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
  }
}
