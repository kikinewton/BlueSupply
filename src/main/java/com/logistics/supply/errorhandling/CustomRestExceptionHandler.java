package com.logistics.supply.errorhandling;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@ControllerAdvice
public class CustomRestExceptionHandler extends ResponseEntityExceptionHandler {

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      final MethodArgumentNotValidException ex,
      final HttpHeaders headers,
      final HttpStatus status,
      final WebRequest request) {
    log.info(ex.getClass().getName());

    final List<String> errors = new ArrayList<>();
    for (final FieldError error : ex.getBindingResult().getFieldErrors()) {
      errors.add(error.getField() + ": " + error.getDefaultMessage());
    }
    for (final ObjectError error : ex.getBindingResult().getGlobalErrors()) {
      errors.add(error.getObjectName() + ": " + error.getDefaultMessage());
    }
    final ApiError apiError =
        new ApiError(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), errors);

    return handleExceptionInternal(ex, apiError, headers, apiError.getStatus(), request);
  }

  @Override
  protected ResponseEntity<Object> handleBindException(
      final BindException ex,
      final HttpHeaders headers,
      final HttpStatus status,
      final WebRequest request) {
    log.info(ex.getClass().getName());

    final List<String> errors = new ArrayList<String>();
    for (final FieldError error : ex.getBindingResult().getFieldErrors()) {
      errors.add(error.getField() + ": " + error.getDefaultMessage());
    }
    for (final ObjectError error : ex.getBindingResult().getGlobalErrors()) {
      errors.add(error.getObjectName() + ": " + error.getDefaultMessage());
    }
    final ApiError apiError =
        new ApiError(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), errors);
    return handleExceptionInternal(ex, apiError, headers, apiError.getStatus(), request);
  }

  @Override
  protected ResponseEntity<Object> handleTypeMismatch(
      final TypeMismatchException ex,
      final HttpHeaders headers,
      final HttpStatus status,
      final WebRequest request) {
    log.info(ex.getClass().getName());
    final String error =
        ex.getValue()
            + " value for "
            + ex.getPropertyName()
            + " should be of type "
            + ex.getRequiredType();

    final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), error);
    return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
  }

  @Override
  protected ResponseEntity<Object> handleMissingServletRequestPart(
      final MissingServletRequestPartException ex,
      final HttpHeaders headers,
      final HttpStatus status,
      final WebRequest request) {
    log.info(ex.getClass().getName());
    //
    final String error = ex.getRequestPartName() + " part is missing";
    final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), error);
    return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
  }

  @Override
  protected ResponseEntity<Object> handleMissingServletRequestParameter(
      final MissingServletRequestParameterException ex,
      final HttpHeaders headers,
      final HttpStatus status,
      final WebRequest request) {
    log.info(ex.getClass().getName());
    //
    final String error = ex.getParameterName() + " parameter is missing";
    final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), error);
    return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
  }

  //

  @ExceptionHandler({MethodArgumentTypeMismatchException.class})
  public ResponseEntity<Object> handleMethodArgumentTypeMismatch(
      final MethodArgumentTypeMismatchException ex, final WebRequest request) {
    log.info(ex.getClass().getName());
    //
    final String error = ex.getName() + " should be of type " + ex.getRequiredType().getName();

    final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), error);
    return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
  }

  @ExceptionHandler({ConstraintViolationException.class})
  public ResponseEntity<Object> handleConstraintViolation(
      final ConstraintViolationException ex, final WebRequest request) {
    log.info(ex.getClass().getName());

    final List<String> errors = new ArrayList<>();
    for (final ConstraintViolation<?> violation : ex.getConstraintViolations()) {
      errors.add(
          violation.getRootBeanClass().getName()
              + " "
              + violation.getPropertyPath()
              + ": "
              + violation.getMessage());
    }

    final ApiError apiError =
        new ApiError(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), errors);
    return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
  }

  @ExceptionHandler({DataIntegrityViolationException.class})
  public ResponseEntity<Object> handleDataIntegrityViolationConflict(
          final DataIntegrityViolationException ex, final WebRequest request) {
    log.info(ex.getClass().getName());

    final String error = ex.getMostSpecificCause().getLocalizedMessage();
    final ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), error);
    return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
  }

  // 404

  @Override
  protected ResponseEntity<Object> handleNoHandlerFoundException(
      final NoHandlerFoundException ex,
      final HttpHeaders headers,
      final HttpStatus status,
      final WebRequest request) {
    final String error = "No handler found for " + ex.getHttpMethod() + " " + ex.getRequestURL();

    final ApiError apiError = new ApiError(HttpStatus.NOT_FOUND, ex.getLocalizedMessage(), error);
    return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
  }

  // 405

  @Override
  protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
      final HttpRequestMethodNotSupportedException ex,
      final HttpHeaders headers,
      final HttpStatus status,
      final WebRequest request) {
    log.info(ex.getClass().getName());
    //
    final StringBuilder builder = new StringBuilder();
    builder.append(ex.getMethod());
    builder.append(" method is not supported for this request. Supported methods are ");
    ex.getSupportedHttpMethods().forEach(t -> builder.append(t + " "));

    final ApiError apiError =
        new ApiError(HttpStatus.METHOD_NOT_ALLOWED, ex.getLocalizedMessage(), builder.toString());
    return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
  }

  // 415

  @Override
  protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
      final HttpMediaTypeNotSupportedException ex,
      final HttpHeaders headers,
      final HttpStatus status,
      final WebRequest request) {
    log.info(ex.getClass().getName());
    //
    final StringBuilder builder = new StringBuilder();
    builder.append(ex.getContentType());
    builder.append(" media type is not supported. Supported media types are ");
    ex.getSupportedMediaTypes().forEach(t -> builder.append(t + " "));

    final ApiError apiError =
        new ApiError(
            HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            ex.getLocalizedMessage(),
            builder.substring(0, builder.length() - 2));
    return new ResponseEntity<Object>(apiError, new HttpHeaders(), apiError.getStatus());
  }

  @ExceptionHandler({BadCredentialsException.class})
  protected ResponseEntity<?> handleBadCredentials(final BadCredentialsException e) {
    log.info(e.getClass().getName());
    final ApiError apiError =
        new ApiError(HttpStatus.NOT_FOUND, e.getLocalizedMessage(), "INVALID_CREDENTIALS");
    return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
  }

  @ExceptionHandler({org.springframework.security.access.AccessDeniedException.class})
  public ResponseEntity<?> handleAccessDenied(
      final org.springframework.security.access.AccessDeniedException e) {
    log.info(e.getClass().getName());
    final ApiError apiError =
        new ApiError(HttpStatus.FORBIDDEN, e.getLocalizedMessage(), "ACCESS_DENIED");
    return new ResponseEntity<>(apiError, new HttpHeaders(), apiError.getStatus());
  }

  // 500

  @ExceptionHandler({Exception.class})
  public ResponseEntity<Object> handleAll(final Exception ex, final WebRequest request) {
    log.error("error", ex);
    final ApiError apiError =
        new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage(), ex.getCause().getMessage());
    return new ResponseEntity<Object>(apiError, new HttpHeaders(), apiError.getStatus());
  }

  @ExceptionHandler({GeneralException.class})
  public ResponseEntity<Object> handleAllGeneralException(final GeneralException ex, final WebRequest request) {
    log.error("error", ex);

    final ApiError apiError =
            new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getLocalizedMessage(), ex.getCause().getMessage());
    return new ResponseEntity<Object>(apiError, new HttpHeaders(), apiError.getStatus());
  }
}
