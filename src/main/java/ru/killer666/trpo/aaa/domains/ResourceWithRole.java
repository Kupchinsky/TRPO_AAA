package ru.killer666.trpo.aaa.domains;

import lombok.Data;
import lombok.ToString;

import javax.persistence.*;

@Data
@ToString

@Entity
@Table(name = "resources_users")
public class ResourceWithRole {
    @Id
    @Column(name = "id")
    @GeneratedValue
    private Integer databaseId;

    @ManyToOne
    @JoinColumn(name = "resource_id", referencedColumnName = "id", nullable = false)
    private Resource resource;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @Column(name = "role", nullable = false)
    private Integer role;
}