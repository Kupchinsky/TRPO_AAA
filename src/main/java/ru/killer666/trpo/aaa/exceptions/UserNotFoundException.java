package ru.killer666.trpo.aaa.exceptions;

public class UserNotFoundException extends ExceptionWithData {
    public UserNotFoundException(String userName) {
        this.setCauseUserName(userName);
    }
}

