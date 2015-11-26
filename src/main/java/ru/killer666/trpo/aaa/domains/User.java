package ru.killer666.trpo.aaa.domains;

import lombok.Data;
import lombok.ToString;

import javax.persistence.*;

@Data
@ToString
@Entity
@Table(name = "users")
public class User {
    @Id
    @Column(name = "id")
    @GeneratedValue
    private int databaseId;

    @Column(name = "login", unique = true, nullable = false)
    private String login;

    @Column(name = "passwordHash", nullable = false)
    private String passwordHash;

    @Column(name = "salt", nullable = false)
    private String salt;

    @Column(name = "personName", nullable = false)
    private String personName;
}
