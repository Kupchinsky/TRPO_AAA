package ru.killer666.trpo.aaa.views;

import lombok.Getter;
import lombok.ToString;
import ru.killer666.trpo.aaa.RoleInterface;

@ToString
public enum Role implements RoleInterface {
    READ(1), WRITE(2), EXECUTE(4);

    @Getter
    private final int value;

    Role(int value) {
        this.value = value;
    }
}
