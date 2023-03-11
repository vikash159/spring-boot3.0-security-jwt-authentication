package com.chat.exception;

import com.chat.payload.Result;
import jakarta.validation.ConstraintViolationException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.stream.Collectors;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException ex) {
        String message = ex.getMessage();
        String msg[] = message.split("Detail:");
        message = msg[0].trim();
        if (msg.length > 1) message = msg[1].trim();

        Result result = new Result();
        result.setMessage(message);
        result.setSuccess(false);
        return ResponseEntity.ok(result);
    }

    @ExceptionHandler({AppException.class, BadCredentialsException.class, NullPointerException.class})
    protected ResponseEntity<Object> handleAppException(RuntimeException ex) {
        Result result = new Result();
        result.setSuccess(false);
        result.setMessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        BindingResult result = ex.getBindingResult();
        String errorMessages = result.getAllErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.joining(","));

        Result res = new Result();
        res.setSuccess(false);
        res.setMessage(errorMessages);
        return ResponseEntity.ok(res);
    }
}
