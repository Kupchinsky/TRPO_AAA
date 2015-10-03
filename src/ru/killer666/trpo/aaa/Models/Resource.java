package ru.killer666.trpo.aaa.Models;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class Resource {
    @NonNull
    private int databaseId;
    @NonNull
    private String name;
    private Resource parentResource = null;
}
