package ru.killer666.trpo.aaa.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class InvalidRoleException extends Exception {
    @Getter
    private String causeStr;
}