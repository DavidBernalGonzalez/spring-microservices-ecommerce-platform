package com.microservice.order.exceptions;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiFieldError {

    private String field;
    private String message;
    private List<String> allowedValues;
    private List<String> rejectedValues;

    public ApiFieldError(String field, String message) {
        this.field = field;
        this.message = message;
        this.allowedValues = null;
        this.rejectedValues = null;
    }
}