package ru.killer666.trpo.aaa.exceptions;

import lombok.Getter;
import lombok.Setter;

public abstract class ExceptionWithData extends Exception {
    @Getter
    @Setter
    private String causeUserName = null;

    @Getter
    @Setter
    private String causePassword = null;

    @Getter
    @Setter
    private String causeResource = null;
}