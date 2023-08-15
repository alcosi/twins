package org.twins.core.dao.twinclass;

import lombok.Data;

import jakarta.persistence.*;
import org.cambium.i18n.dao.I18nEntity;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.user.UserEntity;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
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

    @Column(name = "name_i18n_id")
    private UUID nameI18NId;

    @Column(name = "description_i18n_id")
    private UUID descriptionI18NId;

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @ManyToOne
    @JoinColumn(name = "domain_id", insertable = false, updatable = false)
    private DomainEntity domain;

    @ManyToOne
    @JoinColumn(name = "name_i18n_id", insertable = false, updatable = false)
    private I18nEntity i18NByNameI18NId;

    @ManyToOne
    @JoinColumn(name = "description_i18n_id", insertable = false, updatable = false)
    private I18nEntity i18NByDescriptionI18NId;

    @ManyToOne
    @JoinColumn(name = "created_by_user_id", insertable = false, updatable = false, nullable = false)
    private UserEntity createdByUser;
}
