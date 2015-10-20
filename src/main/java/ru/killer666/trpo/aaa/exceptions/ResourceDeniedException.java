package ru.killer666.trpo.aaa.exceptions;

public class ResourceDeniedException extends ExceptionWithData {
    public ResourceDeniedException(String resourceName, String userName) {
        this.setCauseResource(resourceName);
        this.setCauseUserName(userName);
    }
}
