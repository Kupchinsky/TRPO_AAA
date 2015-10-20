package ru.killer666.trpo.aaa.domains;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@AllArgsConstructor
@Data
@ToString
public class User {
    private int databaseId;
    private String login;
    private String passwordHash;
    private String salt;
    private String personName;
}
