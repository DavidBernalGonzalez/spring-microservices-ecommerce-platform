package com.microservice.product.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiFieldError {

    private String field;
    private String message;

}