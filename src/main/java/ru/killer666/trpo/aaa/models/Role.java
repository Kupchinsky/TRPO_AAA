package ru.killer666.trpo.aaa.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@ToString
public enum Role {
    READ(1), WRITE(2), EXECUTE(4);

    @Getter
    private final int value;

    Role(int value) {
        this.value = value;
    }

    public static Role fromInt(int x) throws InvalidRoleException {
        switch (x) {
            case 1:
                return READ;
            case 2:
                return WRITE;
            case 4:
                return EXECUTE;
        }

        throw new InvalidRoleException(null);
    }

    @AllArgsConstructor
    public static class InvalidRoleException extends Exception {
        @Getter
        private String causeStr;
    }
}
