package ru.killer666.trpo.aaa.Models;

import lombok.Data;

import java.util.Date;

@Data
public class Accounting {
    private int databaseId;
    private int roles;
    private int volume;
    private Date loginDate;
    private Date logoutDate;
}
