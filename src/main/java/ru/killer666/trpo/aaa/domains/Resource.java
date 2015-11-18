package ru.killer666.trpo.aaa.domains;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@AllArgsConstructor
@Data
@ToString
public class Resource implements Comparable<Resource> {
    private int databaseId;
    private String name;
    private Resource parentResource;

    @Override
    public int compareTo(Resource other) {
        return other.getName().compareTo(this.getName());
    }
}
