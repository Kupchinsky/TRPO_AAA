package ru.killer666.trpo.aaa.exceptions;

public class ResourceNotFoundException extends ExceptionWithData {
    public ResourceNotFoundException(String resourceName) {
        this.setCauseResource(resourceName);
    }
}
