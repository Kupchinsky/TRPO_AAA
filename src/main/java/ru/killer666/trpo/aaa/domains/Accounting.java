package ru.killer666.trpo.aaa.domains;

import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Data
@ToString

@Entity
@Table(name = "accounting")
public class Accounting {
    @Id
    @Column(name = "id")
    @GeneratedValue
    private Integer databaseId;

    @Column(name = "user_id")
    private User user;

    @OneToMany
    private List<AccountingResource> resources = new ArrayList<>();

    @Column(name = "volume")
    private Integer volume = 0;

    @Column(name = "logon_date")
    private Date loginDate = Calendar.getInstance().getTime();

    @Column(name = "logout_date")
    private Date logoutDate = null;

    public void increaseVolume(int add) {
        this.volume += add;
    }
}
