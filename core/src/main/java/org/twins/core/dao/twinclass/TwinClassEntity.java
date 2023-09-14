package org.twins.core.dao.twinclass;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.i18n.dao.I18nEntity;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.user.UserEntity;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Accessors(fluent = true)
@Table(name = "twin_class")
public class TwinClassEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "key")
    private String key;

    @Column(name = "space")
    private Boolean space;

    @Column(name = "abstract")
    private Boolean abstractt;

    @Column(name = "name_i18n_id")
    private UUID nameI18NId;

    @Column(name = "description_i18n_id")
    private UUID descriptionI18NId;

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "logo")
    private String logo;

    @Column(name = "head_twin_class_id")
    private UUID headTwinClassId;

    @ManyToOne
    @JoinColumn(name = "domain_id", insertable = false, updatable = false)
    private DomainEntity domain;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "name_i18n_id", insertable = false, updatable = false)
    private I18nEntity nameI18n;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "description_i18n_id", insertable = false, updatable = false)
    private I18nEntity descriptionI18n;

    @ManyToOne
    @JoinColumn(name = "created_by_user_id", insertable = false, updatable = false, nullable = false)
    private UserEntity createdByUser;

    public String logShort() {
        return "twinClass[id:" + id + ", key:" + key + "]";
    }
}
