package ru.killer666.trpo.aaa.models;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
@Data
@ToString
public class Accounting {
    @NonNull
    private User user;
    private List<Resource> resources = new ArrayList<>();
    @NonNull
    private Role role;
    private int volume = 0;
    private Date loginDate = null;
    private Date logoutDate = null;
}
