package ru.killer666.trpo.aaa.domains;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@AllArgsConstructor
@Data
@ToString
public class Resource {
    private int databaseId;
    private String name;
    private Resource parentResource;
}
