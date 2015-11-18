package ru.killer666.trpo.aaa.domains;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
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

    private Multimap<Resource, RoleInterface> resources = ArrayListMultimap.create();

    private int volume = 0;

    private Date loginDate = Calendar.getInstance().getTime();
    private Date logoutDate = null;

    public void increaseVolume(int add) {
        this.volume += add;
    }
}
