package org.twins.core.dao.domain;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.featurer.dao.FeaturerEntity;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;
import org.twins.core.dao.face.FaceEntity;
import org.twins.core.dao.i18n.LocaleConverter;
import org.twins.core.dao.idp.IdentityProviderEntity;
import org.twins.core.dao.notification.NotificationSchemaEntity;
import org.twins.core.dao.permission.PermissionSchemaEntity;
import org.twins.core.dao.resource.ResourceEntity;
import org.twins.core.dao.resource.StorageEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassSchemaEntity;
import org.twins.core.enums.domain.DomainStatus;
import org.twins.core.enums.domain.DomainType;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Entity
@Table(name = "domain")
@DynamicUpdate
@Data
@FieldNameConstants
@Accessors(chain = true)
public class DomainEntity implements EasyLoggable {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "key")
    private String key;

    @Column(name = "domain_status_id")
    @Enumerated(EnumType.STRING)
    private DomainStatus domainStatusId;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "permission_schema_id")
    private UUID permissionSchemaId;

    @Column(name = "twinflow_schema_id")
    private UUID twinflowSchemaId;

    @Column(name = "twin_class_schema_id")
    private UUID twinClassSchemaId;

    @Column(name = "business_account_template_twin_id")
    private UUID businessAccountTemplateTwinId;

    @Column(name = "default_tier_id")
    private UUID defaultTierId;

    @Column(name = "domain_user_template_twin_id")
    private UUID domainUserTemplateTwinId;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "default_i18n_locale_id")
    @Convert(converter = LocaleConverter.class)
    private Locale defaultI18nLocaleId;

    @Column(name = "ancestor_twin_class_id")
    private UUID ancestorTwinClassId;

    @Column(name = "business_account_initiator_featurer_id")
    private Integer businessAccountInitiatorFeaturerId;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "business_account_initiator_params", columnDefinition = "hstore")
    private HashMap<String, String> businessAccountInitiatorParams;

    @Column(name = "domain_user_initiator_featurer_id")
    private Integer domainUserInitiatorFeaturerId;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "domain_user_initiator_params", columnDefinition = "hstore")
    private HashMap<String, String> domainUserInitiatorParams;

    @Column(name = "attachments_storage_used_count")
    private Long attachmentsStorageUsedCount;

    @Column(name = "attachments_storage_used_size")
    private Long attachmentsStorageUsedSize;

    @Column(name = "user_group_manager_featurer_id")
    private Integer userGroupManagerFeaturerId;

    @Column(name = "icon_light_resource_id")
    private UUID iconLightResourceId;

    @Column(name = "icon_dark_resource_id")
    private UUID iconDarkResourceId;

    @Column(name = "attachments_storage_id")
    private UUID attachmentsStorageId;

    @Column(name = "resources_storage_id")
    private UUID resourcesStorageId;

    @Column(name = "navbar_face_id")
    private UUID navbarFaceId;

    @Column(name = "identity_provider_id")
    private UUID identityProviderId;

    @Column(name = "notification_schema_id")
    private UUID notificationSchemaId;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FeaturerEntity userGroupManagerFeaturer;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "user_group_manager_params", columnDefinition = "hstore")
    private HashMap<String, String> userGroupManagerParams;

    @Column(name = "domain_type_id")
    @Convert(converter = DomainTypeConverter.class)
    private DomainType domainType;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "icon_light_resource_id", insertable = false, updatable = false)
    private ResourceEntity iconLightResource;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "icon_dark_resource_id", insertable = false, updatable = false)
    private ResourceEntity iconDarkResource;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attachments_storage_id", insertable = false, updatable = false)
    private StorageEntity attachmentsStorage;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resources_storage_id", insertable = false, updatable = false)
    private StorageEntity resourcesStorage;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "navbar_face_id", insertable = false, updatable = false)
    private FaceEntity navbarFace;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "identity_provider_id", insertable = false, updatable = false)
    private IdentityProviderEntity identityProvider;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_class_schema_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinClassSchemaEntity twinClassSchema;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_schema_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private PermissionSchemaEntity permissionSchema;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_account_template_twin_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinEntity businessAccountTemplateTwin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_tier_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TierEntity defaultTier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_user_template_twin_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinEntity domainUserTemplateTwin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_schema_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    private NotificationSchemaEntity notificationSchema;

    // needed for specification
    @Deprecated
    @OneToMany(mappedBy = "domain", fetch = FetchType.LAZY)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private List<DomainBusinessAccountEntity> domainBusinessAccounts;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private DomainTypeEntity domainTypeEntity;

    public String easyLog(Level level) {
        return "domain[id:" + id + ", key:" + key + "]";
    }
}
