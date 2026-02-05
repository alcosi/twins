package org.twins.core.dao.error;

import jakarta.persistence.*;
import lombok.Data;
import org.cambium.common.util.UuidUtils;
import org.twins.core.dao.domain.DomainVersionEntity;
import org.twins.core.domain.versioning.DomainSetting;

import java.util.UUID;

@Entity
@Table(name = "error")
@DomainSetting
@Data
public class ErrorEntity {
    @Id
    private UUID id;

    @Column(name = "domain_version_id")
    private UUID domainVersionId;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Basic
    @Column(name = "code_local")
    public int errorCodeLocal;

    @Basic
    @Column(name = "code_external")
    public String errorCodeExternal;

    @Basic
    @Column(name = "name")
    private String name;

    @Basic
    @Column(name = "description")
    private String description;

    @Basic
    @Column(name = "client_msg_i18n_id")
    private UUID clientMsgI18nId;

    // @ManyToOne(fetch = FetchType.EAGER)
    // @JoinColumn(name = "client_msg_i18n_id", nullable = false, insertable =
    // false, updatable = false)
    // private I18nEntity clientMsgI18n;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_version_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private DomainVersionEntity domainVersion;
}
