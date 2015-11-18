package ru.killer666.trpo.aaa.domains;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Data
@ToString
public class Accounting {
    @NonNull
    private User user;

    private Map<Resource, RoleInterface> resources = new HashMap<>();

    private int volume = 0;

    private Date loginDate = Calendar.getInstance().getTime();
    private Date logoutDate = null;

    public void increaseVolume(int add) {
        this.volume += add;
    }
}
