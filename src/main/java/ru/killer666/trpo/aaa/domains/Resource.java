package ru.killer666.trpo.aaa.domains;

import com.google.gson.annotations.Expose;
import lombok.Data;
import lombok.ToString;

import javax.persistence.*;

@Data
@ToString
@Entity
@Table(name = "resources")
public class Resource implements Comparable<Resource> {
    @Id
    @Column(name = "id")
    @GeneratedValue
    @Expose(deserialize = false)
    private int databaseId;

    @Column(name = "name", unique = true, nullable = false)
    @Expose
    private String name;

    @ManyToOne
    @JoinColumn(name = "parent_resource_id", referencedColumnName = "id")
    private Resource parentResource;

    @Override
    public int compareTo(Resource o) {
        return o.getName().compareTo(this.getName());
    }
}
