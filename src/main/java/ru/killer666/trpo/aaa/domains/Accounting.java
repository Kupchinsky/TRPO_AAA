package ru.killer666.trpo.aaa.domains;

import lombok.Data;
import lombok.NonNull;
import lombok.ToString;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Data
@ToString
public class Accounting {
    @Id
    @Column(name = "id")
    @GeneratedValue
    private int databaseId;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @NonNull
    private List<AccountingResource> resources = new ArrayList<>();

    @Column(name = "volume")
    private int volume = 0;

    @Column(name = "logon_date")
    private Date loginDate = Calendar.getInstance().getTime();

    @Column(name = "logout_date")
    private Date logoutDate = null;

    public void increaseVolume(int inc) {
        this.volume += inc;
    }
}
