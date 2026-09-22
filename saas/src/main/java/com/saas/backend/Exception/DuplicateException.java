package com.saas.backend.Exception;

public class DuplicateException extends RuntimeException{
    public DuplicateException(String message){
        super(message);
    } 
}
