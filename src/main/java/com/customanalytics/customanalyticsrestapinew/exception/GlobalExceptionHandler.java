package com.customanalytics.customanalyticsrestapinew.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public String handleUserNotFoundException(UserNotFoundException ex){
        return ex.getMessage();
    }

    @ExceptionHandler(IndexNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public String handleIndexNotFoundException(IndexNotFoundException ex){
        return ex.getMessage();
    }

    @ExceptionHandler(IndexAlreadyExistException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public String handleIndexAlreadyExistException(IndexAlreadyExistException ex){
        return ex.getMessage();
    }
}
