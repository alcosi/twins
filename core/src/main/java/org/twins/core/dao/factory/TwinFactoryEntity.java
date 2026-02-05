package org.twins.core.dao.factory;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.UuidUtils;
import org.hibernate.annotations.CreationTimestamp;
import org.twins.core.dao.domain.DomainVersionEntity;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.versioning.DomainSetting;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
@Entity
@DomainSetting
@Table(name = "twin_factory")
public class TwinFactoryEntity implements EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "key")
    private String key;

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "domain_version_id")
    private UUID domainVersionId;

    @Column(name = "name_i18n_id")
    private UUID nameI18NId;

    @Column(name = "description_i18n_id")
    private UUID descriptionI18NId;

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Timestamp createdAt;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "created_by_user_id", insertable = false, updatable = false)
    private UserEntity createdByUser;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "name_i18n_id", insertable = false, updatable = false)
    private I18nEntity nameI18n;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "description_i18n_id", insertable = false, updatable = false)
    @JoinColumn(name = "description_i18n_id", insertable = false, updatable = false)
    private I18nEntity descriptionI18n;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_version_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private DomainVersionEntity domainVersion;

    @Transient
    public Integer factoryUsagesCount;

    @Transient
    public Integer factoryPipelinesCount;

    @Transient
    public Integer factoryMultipliersCount;

    @Transient
    public Integer factoryBranchesCount;

    @Transient
    public Integer factoryErasersCount;

    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinFactory[" + id + "]";
            default -> "twinFactory[id:" + id + ", key:" + key + "]";
        };
    }

}
