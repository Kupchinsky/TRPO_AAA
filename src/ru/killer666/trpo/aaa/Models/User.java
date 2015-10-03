package ru.killer666.trpo.aaa.Models;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class User {
    private int databaseId;
    private String login;
    private String passwordHash;
    private String salt;
    private int roles;
    private String personName;
}
