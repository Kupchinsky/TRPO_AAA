package ru.killer666.trpo.aaa.exceptions;

public class IncorrectPasswordException extends ExceptionWithData {
    public IncorrectPasswordException(String userName, String password) {
        this.setCauseUserName(userName);
        this.setCausePassword(password);
    }
}
