package ru.killer666.trpo.aaa.domains;

import lombok.Data;
import lombok.ToString;

import javax.persistence.*;

@Data
@ToString

@Entity
@Table(name = "resources_users", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "resource_id", "role"}))
public class ResourceWithRole {
    @Id
    @Column(name = "id")
    @GeneratedValue
    private int databaseId;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "resource_id", referencedColumnName = "id")
    private Resource parentResource;

    @Column(name = "role")
    private int role;
}