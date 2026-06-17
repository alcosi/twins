package org.twins.core.dao.link;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.UuidUtils;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.twins.core.dao.i18n.I18nTranslationEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.enums.link.LinkStrength;
import org.twins.core.enums.link.LinkType;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Data
@FieldNameConstants
@Accessors(chain = true)
@Entity
@Table(name = "link")
public class LinkEntity implements EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "src_twin_class_id")
    private UUID srcTwinClassId;

    @Column(name = "src_twin_class_inheritable")
    private Boolean srcTwinClassInheritable;

    @Column(name = "dst_twin_class_id")
    private UUID dstTwinClassId;

    @Column(name = "dst_twin_class_inheritable")
    private Boolean dstTwinClassInheritable;

    @Column(name = "forward_name_i18n_id")
    private UUID forwardNameI18NId;

    @Column(name = "backward_name_i18n_id")
    private UUID backwardNameI18NId;

    @Column(name = "link_type_id")
    @Enumerated(EnumType.STRING)
    private LinkType type;

    @Column(name = "link_strength_id")
    @Enumerated(EnumType.STRING)
    private LinkStrength linkStrengthId;

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "linker_featurer_id")
    private Integer linkerFeaturerId;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "linker_params", columnDefinition = "hstore")
    private HashMap<String, String> linkerParams;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "src_twin_class_id", insertable = false, updatable = false, nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinClassEntity srcTwinClass;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "dst_twin_class_id", insertable = false, updatable = false, nullable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinClassEntity dstTwinClass;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private UserEntity createdByUserSpecOnly;

    // Direct join to i18n_translation by raw FK — skips intermediate i18n table
    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "i18n_id", referencedColumnName = "forward_name_i18n_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private List<I18nTranslationEntity> forwardNameI18nTranslationsSpecOnly;

    // Direct join to i18n_translation by raw FK — skips intermediate i18n table
    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "i18n_id", referencedColumnName = "backward_name_i18n_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private List<I18nTranslationEntity> backwardNameI18nTranslationsSpecOnly;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private UserEntity createdByUser;

    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "link[" + id + "]";
            default ->
                    "link[id:" + id + ", srcTwinClassId:" + srcTwinClassId + "], dstTwinClassId:" + dstTwinClassId + "]";
        };

    }
}
