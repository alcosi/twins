package org.twins.core.dao.twinclass;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.featurer.dao.FeaturerEntity;
import org.hibernate.annotations.Type;
import org.twins.core.dao.LtreeUserType;
import org.twins.core.dao.action.TwinActionPermissionEntity;
import org.twins.core.dao.attachment.TwinAttachmentActionAlienPermissionEntity;
import org.twins.core.dao.attachment.TwinAttachmentRestrictionEntity;
import org.twins.core.dao.comment.TwinCommentActionAlienPermissionEntity;
import org.twins.core.dao.comment.TwinCommentActionSelfEntity;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dao.face.FaceEntity;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dao.resource.ResourceEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinflow.TwinflowEntity;
import org.twins.core.dao.twinflow.TwinflowTransitionEntity;
import org.twins.core.dao.validator.TwinActionValidatorRuleEntity;
import org.twins.core.dao.validator.TwinAttachmentActionAlienValidatorRuleEntity;
import org.twins.core.dao.validator.TwinAttachmentActionSelfValidatorRuleEntity;
import org.twins.core.dao.validator.TwinCommentActionAlienValidatorRuleEntity;
import org.twins.core.enums.action.TwinAction;
import org.twins.core.enums.attachment.TwinAttachmentAction;
import org.twins.core.enums.comment.TwinCommentAction;
import org.twins.core.enums.twinclass.OwnerType;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorage;

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
            this.id = UUID.nameUUIDFromBytes((key + domainId).getBytes());
        }
    }

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "key")
    private String key;

    @Column(name = "twin_class_freeze_id")
    private UUID twinClassFreezeId;

    @Column(name = "permission_schema_space")
    private Boolean permissionSchemaSpace;

    @Column(name = "twinflow_schema_space")
    private Boolean twinflowSchemaSpace;

    @Column(name = "twin_class_schema_space")
    private Boolean twinClassSchemaSpace;

    @Column(name = "alias_space")
    private Boolean aliasSpace;

    @Column(name = "view_permission_id")
    private UUID viewPermissionId;

    @Column(name = "create_permission_id")
    private UUID createPermissionId;

    @Column(name = "edit_permission_id")
    private UUID editPermissionId;

    @Column(name = "delete_permission_id")
    private UUID deletePermissionId;

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

    @Column(name = "icon_light_resource_id")
    private UUID iconLightResourceId;

    @Column(name = "icon_dark_resource_id")
    private UUID iconDarkResourceId;

    @Column(name = "head_twin_class_id")
    private UUID headTwinClassId;

    @Column(name = "segment")
    private Boolean segment;

    @Column(name = "has_segments")
    private Boolean hasSegment;

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

    @Column(name = "head_hunter_featurer_id")
    private Integer headHunterFeaturerId;

    @Column(name = "assignee_required")
    private Boolean assigneeRequired;

    @Column(name = "has_dynamic_markers")
    private Boolean hasDynamicMarkers;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FeaturerEntity headHunterFeaturer;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "head_hunter_featurer_params", columnDefinition = "hstore")
    private HashMap<String, String> headHunterParams;

    @Column(name = "page_face_id")
    private UUID pageFaceId;

    @Column(name = "bread_crumbs_face_id")
    private UUID breadCrumbsFaceId;

    @Column(name = "inherited_page_face_id", insertable = false, updatable = false)
    private UUID inheritedPageFaceId;

    @Column(name = "inherited_bread_crumbs_face_id", insertable = false, updatable = false)
    private UUID inheritedBreadCrumbsFaceId;

    @Column(name = "inherited_marker_data_list_id", insertable = false, updatable = false)
    private UUID inheritedMarkerDataListId;

    @Column(name = "inherited_tag_data_list_id", insertable = false, updatable = false)
    private UUID inheritedTagDataListId;

    @Column(name = "general_attachment_restriction_id")
    private UUID generalAttachmentRestrictionId;

    @Column(name = "comment_attachment_restriction_id")
    private UUID commentAttachmentRestrictionId;

    @Column(name = "external_id")
    private String externalId;

    @Column(name = "head_hierarchy_counter_direct_children", nullable = false)
    private Integer headHierarchyCounterDirectChildren;

    @Column(name = "extends_hierarchy_counter_direct_children", nullable = false)
    private Integer extendsHierarchyCounterDirectChildren;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "external_properties", columnDefinition = "hstore")
    private Map<String, String> externalProperties;

    @Type(JsonType.class)
    @Column(name = "external_json", columnDefinition = "jsonb")
    private Map<String, Object> externalJson;

//    @ManyToOne
//    @JoinColumn(name = "domain_id", insertable = false, updatable = false)
//    private DomainEntity domain;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "name_i18n_id", insertable = false, updatable = false)
    @Deprecated //for specification only
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private I18nEntity nameI18n;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "description_i18n_id", insertable = false, updatable = false)
    @Deprecated //for specification only
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private I18nEntity descriptionI18n;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_face_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FaceEntity pageFace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bread_crumbs_face_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FaceEntity breadCrumbsFace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inherited_page_face_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FaceEntity inheritedPageFace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inherited_bread_crumbs_face_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FaceEntity inheritedBreadCrumbsFace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inherited_marker_data_list_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private DataListEntity inheritedMarkerDataList;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inherited_tag_data_list_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private DataListEntity inheritedTagDataList;

//    @ManyToOne
//    @JoinColumn(name = "created_by_user_id", insertable = false, updatable = false, nullable = false)
//    private UserEntity createdByUser;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<UUID> extendedClassIdSet;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<UUID> headHierarchyClassIdSet;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Kit<TwinClassEntity, UUID> headHierarchyChildClassKit;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Kit<TwinClassEntity, UUID> extendsHierarchyChildClassKit;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Kit<TwinflowEntity, UUID> twinflowKit;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Kit<TwinClassFieldEntity, UUID> twinClassFieldKit;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<TwinFieldStorage> fieldStorageSet;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Kit<TwinStatusEntity, UUID> twinStatusKit;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Kit<LinkEntity, UUID> linksKit;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Kit<TwinActionPermissionEntity, TwinAction> actionsProtectedByPermission;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private KitGrouped<TwinActionValidatorRuleEntity, UUID, TwinAction> actionsProtectedByValidatorRules;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Kit<TwinCommentActionAlienPermissionEntity, TwinCommentAction> commentAlienActionsProtectedByPermission;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private KitGrouped<TwinCommentActionAlienValidatorRuleEntity, UUID, TwinCommentAction> commentAlienActionsProtectedByValidatorRules;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Kit<TwinCommentActionSelfEntity, TwinCommentAction> commentSelfActionsRestriction;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Kit<TwinAttachmentActionAlienPermissionEntity, TwinAttachmentAction> attachmentAlienActionsProtectedByPermission;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private KitGrouped<TwinAttachmentActionAlienValidatorRuleEntity, UUID, TwinAttachmentAction> attachmentAlienActionsProtectedByValidatorRules;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private KitGrouped<TwinAttachmentActionSelfValidatorRuleEntity, UUID, TwinAttachmentAction> attachmentSelfActionsRestriction;

    //TODO m.b. move to Twinflow entity? services logic
    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Kit<TwinflowTransitionEntity, UUID> transitionsKit;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinClassFreezeEntity twinClassFreeze;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private PermissionEntity viewPermission;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private PermissionEntity createPermission;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private PermissionEntity editPermission;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private PermissionEntity deletePermission;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinClassEntity headTwinClass;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinClassEntity extendsTwinClass;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private DataListEntity markerDataList;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private DataListEntity tagDataList;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinAttachmentRestrictionEntity generalAttachmentRestriction;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Kit<TwinClassEntity, UUID> segmentTwinsClassKit;


    public Set<UUID> getExtendedClassIdSet() {
        if (null == extendedClassIdSet && null != getExtendsHierarchyTree()) {
            extendedClassIdSet = new LinkedHashSet<>();
            var hierarchyIds = convertUuidFromLtreeFormat(getExtendsHierarchyTree()).split("\\.");
            for (int i = hierarchyIds.length - 1; i >= 0; i--) //reverse direction, directly extends - first
                extendedClassIdSet.add(UUID.fromString(hierarchyIds[i]));
        }
        return extendedClassIdSet;
    }

    public Set<UUID> getHeadHierarchyClassIdSet() {
        if (null == headHierarchyClassIdSet && null != getHeadHierarchyTree()) {
            headHierarchyClassIdSet = new LinkedHashSet<>();
            var hierarchyIds = convertUuidFromLtreeFormat(getHeadHierarchyTree()).split("\\.");
            for (int i = hierarchyIds.length - 1; i >= 0; i--) //reverse direction, directly extends - first
                headHierarchyClassIdSet.add(UUID.fromString(hierarchyIds[i]));
        }
        return headHierarchyClassIdSet;
    }

    public static String convertUuidFromLtreeFormat(String uuidLtreeFormat) {
        return uuidLtreeFormat.replace("_", "-");
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


}
