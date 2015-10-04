package ru.killer666.trpo.aaa.models;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@Data
@ToString
public class Resource {
    @NonNull
    private int databaseId;
    @NonNull
    private String name;
    private Resource parentResource = null;
}
