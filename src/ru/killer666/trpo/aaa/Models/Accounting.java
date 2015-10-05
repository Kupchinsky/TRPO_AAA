package ru.killer666.trpo.aaa.models;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Calendar;
import java.util.Date;

@RequiredArgsConstructor
@Data
@ToString
public class Accounting {
    @NonNull
    private User user;
    @NonNull
    private Resource resource;
    @NonNull
    private Role role;
    private int volume = 0;
    private Date loginDate = Calendar.getInstance().getTime();
    private Date logoutDate = null;
}
