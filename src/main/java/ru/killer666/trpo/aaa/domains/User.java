package ru.killer666.trpo.aaa.domains;

import lombok.Data;
import lombok.ToString;

import javax.persistence.*;

@Data
@ToString

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "login"))
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
