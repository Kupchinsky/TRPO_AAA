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

    @ManyToOne
    @JoinColumn(name = "accounting_id", referencedColumnName = "id", nullable = false)
    private Accounting accounting;

    @ManyToOne
    @JoinColumn(name = "resources_users_id", referencedColumnName = "id", nullable = false)
    private ResourceWithRole resourceWithRole;
}
