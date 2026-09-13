package io.github.absketches.doodlemini.controlleradvice;

import io.github.absketches.doodlemini.exception.ConflictException;
import io.github.absketches.doodlemini.exception.InvalidRequestException;
import io.github.absketches.doodlemini.exception.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class ControllerAdvice {

    @ExceptionHandler(NotFoundException.class)
    ProblemDetail notFound(NotFoundException exception, HttpServletRequest request) {
        return problem(HttpStatus.NOT_FOUND, exception.getMessage(), request);
    }

    @ExceptionHandler(ConflictException.class)
    ProblemDetail conflict(ConflictException exception, HttpServletRequest request) {
        return problem(HttpStatus.CONFLICT, exception.getMessage(), request);
    }

    @ExceptionHandler(InvalidRequestException.class)
    ProblemDetail invalidRequest(InvalidRequestException exception, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail validation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Request validation failed.", request);
        List<String> fieldErrors = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .toList();
        List<String> globalErrors = exception.getBindingResult().getGlobalErrors().stream()
                .map(error -> error.getObjectName() + " " + error.getDefaultMessage())
                .toList();
        List<String> errors = java.util.stream.Stream.concat(fieldErrors.stream(), globalErrors.stream()).toList();
        problem.setProperty("errors", errors);
        return problem;
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    ProblemDetail missingParameter(MissingServletRequestParameterException exception, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "Missing required parameter: " + exception.getParameterName(), request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ProblemDetail typeMismatch(MethodArgumentTypeMismatchException exception, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid value for parameter: " + exception.getName(), request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ProblemDetail unreadableMessage(HttpMessageNotReadableException exception, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "Request body is malformed or contains invalid values.", request);
    }

    private ProblemDetail problem(HttpStatus status, String detail, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(status.getReasonPhrase());
        problem.setProperty("path", request.getRequestURI());
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}
