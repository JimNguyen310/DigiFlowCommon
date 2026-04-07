package fec.digiflow.common.web.exception;

import fec.digiflow.common.dto.BaseResponse;
import fec.digiflow.common.exception.ApplicationException;
import fec.digiflow.common.exception.BaseException;
import fec.digiflow.common.message.GlobalMessage;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Global Exception Handler for unified API error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle custom Application exceptions.
     */
    @ExceptionHandler(ApplicationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseResponse<Void> handleApplicationException(ApplicationException ex, HttpServletRequest request) {
        log.warn("ApplicationException at [{}]: Code: {}, Message: {}, Detail: {}",
                request.getRequestURI(), ex.getCode(), ex.getMessage(), ex.getDetail());
        return BaseResponse.failure(ex);
    }

    /**
     * Handle Base exceptions (if any other custom exceptions extend BaseException).
     */
    @ExceptionHandler(BaseException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseResponse<Void> handleBaseException(BaseException ex, HttpServletRequest request) {
        log.warn("BaseException at [{}]: Code: {}, Message: {}, Detail: {}",
                request.getRequestURI(), ex.getCode(), ex.getMessage(), ex.getDetail());
        return BaseResponse.failure(ex);
    }

    /**
     * Handle Validation errors (@Valid, @Validated).
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseResponse<Void> handleValidationException(BindException ex, HttpServletRequest request) {
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        String errorMessage = fieldErrors.stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        log.warn("ValidationException at [{}]: {}", request.getRequestURI(), errorMessage);
        return BaseResponse.failure(GlobalMessage.BAD_REQUEST, errorMessage);
    }

    /**
     * Handle unexpected generic Exception.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public BaseResponse<Void> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled Exception at [{}]: ", request.getRequestURI(), ex);
        return BaseResponse.failure(GlobalMessage.INTERNAL_SERVER_ERROR, ex.getMessage());
    }
}
