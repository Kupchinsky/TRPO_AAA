package ru.killer666.trpo.aaa.domains;

import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@ToString
public enum Role {
    READ(1), WRITE(2), EXECUTE(4);

    @Getter
    private final int value;

    Role(int value) {
        this.value = value;
    }

    public static String asList() {
        List<String> result = new ArrayList<>();

        for (Role role : Role.values())
            result.add(role.name());

        return String.join(", ", result);
    }
}
