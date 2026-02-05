package org.twins.core.dao.action;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.twins.core.dao.domain.DomainVersionEntity;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.domain.versioning.DomainSetting;

import java.util.UUID;

@Getter
@Setter
@Entity
@DomainSetting
@Table(name = "twin_action")
public class TwinActionEntity {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "name_i18n_id")
    private UUID nameI18nId;

    @Column(name = "domain_version_id")
    private UUID domainVersionId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "name_i18n_id", insertable = false, updatable = false)
    @JoinColumn(name = "name_i18n_id", insertable = false, updatable = false)
    private I18nEntity nameI18nEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_version_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private DomainVersionEntity domainVersion;
}
