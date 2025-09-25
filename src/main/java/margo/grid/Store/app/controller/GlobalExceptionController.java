package margo.grid.Store.app.controller;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import margo.grid.Store.app.dto.ErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionController {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleNotFoundException(EntityNotFoundException e){
        String message = e.getMessage();
        if(message == null)message = "Not found!";
        else if(message.trim().split("\\s+").length == 1)message = "Entity with id " + e.getMessage() + " was not found!";
        return new ResponseEntity<>(new ErrorResponseDto(message), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleConstraintViolationException(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("\n"));
        return ResponseEntity.badRequest().body(new ErrorResponseDto(message));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleMethodArgumentNotValid(MethodArgumentNotValidException e){
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("|\n"));
        return ResponseEntity.badRequest().body(new ErrorResponseDto(message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGlobalException(Exception e){
        return ResponseEntity.internalServerError().body(new ErrorResponseDto("OHHHHHH NOOOOO"));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDto> handleJsonParseError() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponseDto("Invalid JSON format in request body"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleErrorResponses(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(new ErrorResponseDto(e.getMessage()));
    }
}
