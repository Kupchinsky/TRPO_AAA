package ru.killer666.trpo.aaa.domains;

import lombok.Data;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@ToString
public class User {
    @Id
    @Column(name = "id")
    @GeneratedValue
    private int databaseId;

    @Column(name = "login", unique = true)
    private String login;

    @Column(name = "passwordHash")
    private String passwordHash;

    @Column(name = "salt")
    private String salt;

    @Column(name = "personName")
    private String personName;
}
