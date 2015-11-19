package ru.killer666.trpo.aaa.domains;

import lombok.Data;
import lombok.ToString;

import javax.persistence.*;

@Data
@ToString

@Entity
@Table(name = "accounting_resources", uniqueConstraints = @UniqueConstraint(columnNames = {"accounting_id", "resources_users_id"}))
public class AccountingResource {
    @Id
    @Column(name = "id")
    @GeneratedValue
    private Integer databaseId;

    @Column(name = "accounting_id")
    private Accounting accounting;

    @Column(name = "resources_users_id")
    private ResourceWithRole resourceWithRole;
}
