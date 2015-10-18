package ru.killer666.trpo.aaa;

import lombok.Getter;

enum ResultCodes {
    SUCCESS(0), USERNOTFOUND(1), INCORRECTPASSWORD(2), INVALIDROLE(3), RESOURCEDENIED(4), INCORRECTACTIVITY(5), INVALIDINPUT(255);

    @Getter
    private final int value;

    ResultCodes(int i) {
        value = i;
    }
}
