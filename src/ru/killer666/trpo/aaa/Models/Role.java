package ru.killer666.trpo.aaa.models;

import lombok.ToString;

@ToString
public enum Role {
    READ(1), WRITE(2), EXECUTE(4);

    private final int value;

    Role(int value) {
        this.value = value;
    }

    public static Role fromInt(int x) {
        switch (x) {
            case 1:
                return READ;
            case 2:
                return WRITE;
            case 4:
                return EXECUTE;
        }

        return null;
    }
}
