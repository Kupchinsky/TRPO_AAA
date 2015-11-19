package ru.killer666.trpo.aaa.domains;

import lombok.Data;
import lombok.ToString;

import javax.persistence.*;

@Data
@ToString

@Entity
@Table(name = "resources", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
public class Resource implements Comparable<Resource> {
    @Id
    @Column(name = "id")
    @GeneratedValue
    private Integer databaseId;

    @Column(name = "name", unique = true)
    private String name;

    @Column(name = "parent_resource_id")
    private Resource parentResource;

    @Override
    public int compareTo(Resource other) {
        return other.getName().compareTo(this.getName());
    }
}
