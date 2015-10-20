package ru.killer666.trpo.aaa;

import lombok.Getter;

enum ResultCode {
    SUCCESS(0),
    USERNOTFOUND(1),
    INCORRECTPASSWORD(2),
    INVALIDROLE(3),
    RESOURCEDENIED(4),
    INCORRECTACTIVITY(5),
    INVALIDINPUT(255),
    UNKNOWNERROR(255);

    @Getter
    private final int value;

    ResultCode(int i) {
        value = i;
    }
}
