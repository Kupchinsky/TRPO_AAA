package ru.killer666.trpo.aaa.domains;

import com.google.gson.annotations.Expose;
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
    @Expose(deserialize = false)
    private int databaseId;

    @Column(name = "userName", unique = true, nullable = false)
    @Expose
    private String userName;

    @Column(name = "passwordHash", nullable = false)
    private String passwordHash;

    @Column(name = "salt", nullable = false)
    private String salt;

    @Column(name = "personName", nullable = false)
    @Expose
    private String personName;
}
