package org.twins.core.dao.twinclass;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.featurer.annotations.FeaturerList;
import org.cambium.featurer.dao.FeaturerEntity;
import org.hibernate.annotations.Type;
import org.twins.core.dao.LtreeUserType;
import org.twins.core.dao.action.TwinAction;
import org.twins.core.dao.action.TwinClassActionPermissionEntity;
import org.twins.core.dao.action.TwinClassActionValidatorEntity;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinflow.TwinflowEntity;
import org.twins.core.dao.twinflow.TwinflowTransitionEntity;
import org.twins.core.featurer.twinclass.HeadHunter;

import java.sql.Timestamp;
import java.util.*;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "twin_class")
@FieldNameConstants
public class TwinClassEntity implements EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            this.id = UUID.randomUUID();
        }
    }

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "key")
    private String key;

    @Column(name = "permission_schema_space")
    private boolean permissionSchemaSpace;

    @Column(name = "twinflow_schema_space")
    private boolean twinflowSchemaSpace;

    @Column(name = "twin_class_schema_space")
    private boolean twinClassSchemaSpace;

    @Column(name = "alias_space")
    private boolean aliasSpace;

    @Column(name = "view_permission_id")
    private UUID viewPermissionId;

    @Column(name = "abstract")
    private boolean abstractt;

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

    @Column(name = "extends_twin_class_id")
    private UUID extendsTwinClassId;

    @Column(name = "head_hierarchy_tree", columnDefinition = "ltree")
    @Type(value = LtreeUserType.class)
    private String headHierarchyTree;

    @Column(name = "extends_hierarchy_tree", columnDefinition = "ltree")
    @Type(value = LtreeUserType.class)
    private String extendsHierarchyTree;

    @Column(name = "domain_alias_counter")
    private int domainAliasCounter;

    @Column(name = "marker_data_list_id")
    private UUID markerDataListId;

    @Column(name = "tag_data_list_id")
    private UUID tagDataListId;

    @Column(name = "twin_class_owner_type_id")
    @Convert(converter = TwinClassOwnerTypeConverter.class)
    private OwnerType ownerType;

    @FeaturerList(type = HeadHunter.class)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "head_hunter_featurer_id", insertable = false, updatable = false)
    private FeaturerEntity headHunterFeaturer;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "head_hunter_featurer_params", columnDefinition = "hstore")
    private HashMap<String, String> headHunterParams;

//    @ManyToOne
//    @JoinColumn(name = "domain_id", insertable = false, updatable = false)
//    private DomainEntity domain;

//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "name_i18n_id", insertable = false, updatable = false)
//    private I18nEntity nameI18n;

//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "description_i18n_id", insertable = false, updatable = false)
//    private I18nEntity descriptionI18n;

//    @ManyToOne
//    @JoinColumn(name = "created_by_user_id", insertable = false, updatable = false, nullable = false)
//    private UserEntity createdByUser;

    @Transient
    @EqualsAndHashCode.Exclude
    private Set<UUID> extendedClassIdSet;

    @Transient
    @EqualsAndHashCode.Exclude
    private Set<UUID> childClassIdSet;

    @Transient
    @EqualsAndHashCode.Exclude
    private Kit<TwinflowEntity, UUID> twinflowKit;

    @Transient
    @EqualsAndHashCode.Exclude
    private Kit<TwinClassFieldEntity, UUID> twinClassFieldKit;

    @Transient
    @EqualsAndHashCode.Exclude
    private Kit<TwinStatusEntity, UUID> twinStatusKit;

    @Transient
    @EqualsAndHashCode.Exclude
    private Kit<LinkEntity, UUID> linksKit;

    @Transient
    @EqualsAndHashCode.Exclude
    private Kit<TwinClassActionPermissionEntity, TwinAction> actionsProtectedByPermission;

    @Transient
    @EqualsAndHashCode.Exclude
    private KitGrouped<TwinClassActionValidatorEntity, UUID, TwinAction> actionsProtectedByValidator;

    //TODO m.b. move to Twinflow entity? services logic
    @Transient
    @EqualsAndHashCode.Exclude
    private Kit<TwinflowTransitionEntity, UUID> transitionsKit;

    @Transient
    @EqualsAndHashCode.Exclude
    private TwinClassEntity headTwinClass;

    @Transient
    @EqualsAndHashCode.Exclude
    private TwinClassEntity extendsTwinClass;


    public Set<UUID> getExtendedClassIdSet() {
        if (null == extendedClassIdSet) {
            extendedClassIdSet = new HashSet<>();
            for (String hierarchyItem : getExtendsHierarchyTree().replace("_", "-").split("\\."))
                extendedClassIdSet.add(UUID.fromString(hierarchyItem));
        }
        return extendedClassIdSet;
    }

    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinClass[" + key + "]";
            default -> "twinClass[id:" + id + ", key:" + key + "]";
        };

    }

    public boolean isSpace() {
        return permissionSchemaSpace || twinflowSchemaSpace || twinClassSchemaSpace || aliasSpace;
    }

    @Getter
    public enum OwnerType {
        SYSTEM("system", false, false, false),
        USER("user", false, false, true),
        BUSINESS_ACCOUNT("businessAccount", true, false, false),
        DOMAIN("domain", false, true, false),
        DOMAIN_BUSINESS_ACCOUNT("domainBusinessAccount", true, true, false),
        DOMAIN_USER("domainUser", false, true, true),
        DOMAIN_BUSINESS_ACCOUNT_USER("domainBusinessAccountUser", true, true, true);

        private final String id;
        private final boolean businessAccountLevel;
        private final boolean domainLevel;
        private final boolean userLevel;

        OwnerType(String id, boolean businessAccountLevel, boolean domainLevel, boolean userLevel) {
            this.id = id;
            this.businessAccountLevel = businessAccountLevel;
            this.domainLevel = domainLevel;
            this.userLevel = userLevel;
        }

        public static OwnerType valueOd(String type) {
            return Arrays.stream(OwnerType.values()).filter(t -> t.id.equals(type)).findAny().orElse(DOMAIN_BUSINESS_ACCOUNT);
        }

        public boolean isSystemLevel() {
            return this == SYSTEM;
        }
    }


}
