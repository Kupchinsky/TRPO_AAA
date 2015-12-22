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
@Entity
@Table(name = "accounting")
public class Accounting {
    @Id
    @Column(name = "id")
    @GeneratedValue
    private int databaseId;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "accounting")
    private List<AccountingResource> resources = new ArrayList<>();

    @Column(name = "volume", nullable = false)
    private int volume = 0;

    @Column(name = "logon_date", nullable = false)
    private Date loginDate = Calendar.getInstance().getTime();

    @Column(name = "logout_date", nullable = false)
    private Date logoutDate;

    public void increaseVolume(int inc) {
        this.volume += inc;
    }

    public Accounting pushResource(@NonNull ResourceWithRole resourceWithRole) {
        AccountingResource accountingResource = new AccountingResource();

        accountingResource.setAccounting(this);
        accountingResource.setResourceWithRole(resourceWithRole);

        this.getResources().add(accountingResource);

        return this;
    }
}
