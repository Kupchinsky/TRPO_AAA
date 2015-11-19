package ru.killer666.trpo.aaa.domains;

import lombok.Data;
import lombok.ToString;
import ru.killer666.trpo.aaa.RoleInterface;

import javax.persistence.*;

@Data
@ToString

@Entity
@Table(name = "resources_users", uniqueConstraints = @UniqueConstraint(columnNames = {"resource_id", "user_id", "role"}))
public class ResourceWithRole<T extends RoleInterface> {
    @Id
    @Column(name = "id")
    @GeneratedValue
    private Integer databaseId;

    @Column(name = "name", unique = true)
    private String name;

    @Column(name = "parent_resource_id")
    private Resource parentResource;

    @Enumerated
    @Column(name = "role")
    private T role;
}