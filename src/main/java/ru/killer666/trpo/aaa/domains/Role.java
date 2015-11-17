package ru.killer666.trpo.aaa.domains;

import lombok.Getter;
import lombok.ToString;

@ToString
public enum Role implements RoleInterface {
    READ(1), WRITE(2), EXECUTE(4);

    @Getter
    private final int value;

    Role(int value) {
        this.value = value;
    }
}
