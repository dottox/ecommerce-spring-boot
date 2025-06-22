package com.ecommerce.project.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.io.Serial;

public class ApiException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    @Getter
    private HttpStatus status;

    public ApiException() {

    }

    public ApiException(String message) {
        super(message);
    }

    public ApiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }


}
