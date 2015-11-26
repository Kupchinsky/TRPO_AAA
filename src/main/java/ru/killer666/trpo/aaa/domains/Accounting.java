package ru.killer666.trpo.aaa.domains;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
@Data
@ToString
public class Accounting {
    @NonNull
    private User user;
    @NonNull
    private List<AccountingResource> resources = new ArrayList<>();

    private int volume = 0;
    private Date loginDate = Calendar.getInstance().getTime();
    private Date logoutDate = null;

    public void increaseVolume(int inc) {
        this.volume += inc;
    }
}
