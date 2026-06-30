package org.twins.core.dao.twin;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.LTreeUtils;
import org.cambium.common.util.UuidUtils;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.Type;
import org.hibernate.generator.EventType;
import org.twins.core.dao.LtreeUserType;
import org.twins.core.dao.ResettableTransientState;
import org.twins.core.dao.action.ActionRestrictionReasonEntity;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dao.businessaccount.BusinessAccountUserEntity;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.domain.DomainBusinessAccountEntity;
import org.twins.core.dao.domain.DomainUserEntity;
import org.twins.core.dao.face.FaceEntity;
import org.twins.core.dao.permission.PermissionEntity;
import org.twins.core.dao.space.SpaceRoleUserEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinflow.TwinflowEntity;
import org.twins.core.dao.twinflow.TwinflowTransitionEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.Identifiable;
import org.twins.core.domain.TwinAttachmentsCount;
import org.twins.core.domain.field.rule.FieldRulesApplyResult;
import org.twins.core.enums.action.TwinAction;
import org.twins.core.enums.consts.SystemIds;
import org.twins.core.enums.status.StatusType;
import org.twins.core.enums.twin.LoadState;
import org.twins.core.enums.twin.TwinAliasType;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.service.link.TwinLinkService;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

/**
 * JPA entity for a Twin.
 *
 * <p><b>Caching is forbidden.</b> Do NOT cache instances of this class beyond a single
 * HTTP request / transaction boundary. Concretely: never apply {@code @Cacheable},
 * never store instances in static fields, singleton beans, or long-lived collections,
 * and never pass them across {@code @Async} boundaries without copying.
 *
 * <p>Reason: this entity carries request-scoped {@code @Transient} permission state —
 * {@link #twinFieldEditability} and {@link #twinFieldViewability} — populated by
 * {@code TwinService.loadFieldEditability / loadFieldViewability}. These maps hold
 * the per-user, per-field access decision for the current {@code ApiUser}. If an
 * instance leaks to another user (via shared cache, async pool, or any cross-request
 * reuse), that user inherits the original user's permission decisions — a horizontal
 * privilege escalation.
 *
 * <p>Hibernate L1 (persistence context) is transaction-scoped and therefore safe; the
 * risk appears only when application code explicitly caches the entity, returns it
 * from a {@code @RequestScope}/{@code @SessionScope} bean, or hands it to an executor
 * that does not propagate {@code AuthService}'s {@code ThreadLocal ApiUser}.
 *
 * <p>{@link #resetTransientState()} exists for the future case where explicit cleanup
 * is required, but currently has no callers — assume it is not invoked.
 */
@Entity
@Accessors(chain = true)
@Data
@Table(name = "twin")
@FieldNameConstants
@DynamicUpdate
public class TwinEntity implements Cloneable, EasyLoggable, ResettableTransientState, Identifiable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "twin_class_id")
    private UUID twinClassId;

    @Column(name = "head_twin_id")
    private UUID headTwinId;

    @Column(name = "hierarchy_tree", columnDefinition = "ltree")
    @Type(value = LtreeUserType.class)
    private String hierarchyTree;

    @Column(name = "external_id")
    private String externalId;

    @Column(name = "owner_business_account_id")
    private UUID ownerBusinessAccountId;

    @Column(name = "owner_user_id")
    private UUID ownerUserId;

    @Column(name = "view_permission_id")
    private UUID viewPermissionId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "view_permission_id", insertable = false, updatable = false)
    private PermissionEntity viewPermission;

    @Column(name = "view_permission_custom")
    private Boolean viewPermissionCustom = false;

    //materialized
    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(name = "permission_schema_id", updatable = false, insertable = false)
    private UUID permissionSchemaId;

    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(name = "permission_schema_space_id")
    private UUID permissionSchemaSpaceId;

    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(name = "twinflow_schema_space_id")
    private UUID twinflowSchemaSpaceId;

    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(name = "twin_class_schema_space_id")
    private UUID twinClassSchemaSpaceId;

    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(name = "alias_space_id")
    private UUID aliasSpaceId;

    @Column(name = "twin_status_id")
    private UUID twinStatusId;

    @Column(name = "flavor_data_list_option_id")
    private UUID flavorDataListOptionId;

    @Deprecated //for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flavor_data_list_option_id", insertable = false, updatable = false)
    private DataListOptionEntity flavorDataListOptionSpecOnly;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;

    @Column(name = "assigner_user_id")
    private UUID assignerUserId;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "page_face_id")
    private UUID pageFaceId;

    @Column(name = "bread_crumbs_face_id")
    private UUID breadCrumbsFaceId;

    @Column(name = "head_hierarchy_counter_direct_children", insertable = false, updatable = false)
    private Integer headHierarchyCounterDirectChildren;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "twin_class_id", referencedColumnName = "id", insertable = false, updatable = false, nullable = false)
    private TwinClassEntity twinClass;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FaceEntity pageFace;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FaceEntity breadCrumbsFace;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private boolean createElseUpdate = false;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private LoadState markersLoadState = LoadState.NOT_LOADED; // needed for load markers

//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "head_twin_id", referencedColumnName = "id", insertable = false, updatable = false, nullable = true)
//    private TwinEntity headTwin;

//    @ManyToOne
//    @JoinColumn(name = "owner_business_account_id", insertable = false, updatable = false)
//    private BusinessAccountEntity ownerBusinessAccount;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "twin_status_id", insertable = false, updatable = false, nullable = false)
    private TwinStatusEntity twinStatus;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", insertable = false, updatable = false, nullable = false)
    private UserEntity createdByUserSpecOnly;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private UserEntity createdByUser;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "assigner_user_id", insertable = false, updatable = false)
    private UserEntity assignerUser;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "owner_user_id", insertable = false, updatable = false)
    private UserEntity ownerUser;


    //needed for specification
//    @Deprecated
//    @OneToMany(fetch = FetchType.LAZY)
//    @JoinColumn(name = "id", referencedColumnName = "head_twin_id", insertable = false, updatable = false)
//    @EqualsAndHashCode.Exclude
//    private Collection<TwinEntity> childrenTwins;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_schema_space_id", insertable = false, updatable = false)
    private TwinEntity permissionSchemaSpaceSpecOnly;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_id", insertable = false, updatable = false)
    private Collection<TwinTagEntity> tagsSpecOnly;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_id", insertable = false, updatable = false)
    private Collection<TwinMarkerEntity> markersSpecOnly;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "src_twin_id", insertable = false, updatable = false)
    private Collection<TwinLinkEntity> linksBySrcTwinIdSpecOnly;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "dst_twin_id", insertable = false, updatable = false)
    private Collection<TwinLinkEntity> linksByDstTwinIdSpecOnly;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_id", insertable = false, updatable = false)
    private Collection<TwinFieldSimpleEntity> fieldsSimpleSpecOnly;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_id", insertable = false, updatable = false)
    private Collection<SpaceRoleUserEntity> spaceRoleUsersSpecOnly;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_id", insertable = false, updatable = false)
    private Collection<TwinFieldBooleanEntity> fieldsBooleanSpecOnly;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_id", insertable = false, updatable = false)
    private Collection<TwinFieldTimestampEntity> fieldsTimestampSpecOnly;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_id", insertable = false, updatable = false)
    private Collection<TwinFieldDataListEntity> fieldsListSpecOnly;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_id", insertable = false, updatable = false)
    private Collection<TwinFieldUserEntity> fieldsUserSpecOnly;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_id", insertable = false, updatable = false)
    private Collection<TwinFieldTwinClassEntity> fieldsTwinClassListSpecOnly;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_id", insertable = false, updatable = false)
    private Collection<TwinFieldDecimalEntity> fieldsDecimalSpecOnly;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_id", insertable = false, updatable = false)
    private Collection<TwinTouchEntity> touchesSpecOnly;

    @Deprecated // for specification only (search by last change time)
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_id", insertable = false, updatable = false)
    private Collection<TwinLastChangeEntity> lastChangesSpecOnly;

    @Deprecated // for specification only (USER & BA twins)
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Set<DomainUserEntity> domainUsersSpecOnly;

    @Deprecated // for specification only (USER & BA twins)
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Set<BusinessAccountUserEntity> businessAccountUsersUserTwinsSpecOnly;

    @Deprecated // for specification only (USER & BA twins)
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_account_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Set<BusinessAccountUserEntity> businessAccountUsersBusinessAccountTwinsSpecOnly;

    @Deprecated // for specification only (USER & BA twins)
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_account_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Set<DomainBusinessAccountEntity> domainBusinessAccountsSpecOnly;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinEntity spaceTwin;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinEntity headTwin;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinflowEntity twinflow;

    /*
     we have to use TwinClassFieldId as key, not Id. because of case when we load not missing fields
     */
    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Kit<TwinFieldSimpleEntity, UUID> twinFieldSimpleKit;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Kit<TwinFieldSimpleNonIndexedEntity, UUID> twinFieldSimpleNonIndexedKit;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private KitGrouped<TwinFieldI18nEntity, UUID, UUID> twinFieldI18nKit;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Kit<TwinFieldBooleanEntity, UUID> twinFieldBooleanKit;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Kit<TwinFieldTimestampEntity, UUID> twinFieldTimestampKit;

    /*
     we have to use TwinClassFieldId as key, not id. Also, multiple values supported, that is why kit inside a ki
     */
    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private KitGrouped<TwinFieldDataListEntity, UUID, UUID> twinFieldDatalistKit;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private KitGrouped<TwinFieldUserEntity, UUID, UUID> twinFieldUserKit;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private KitGrouped<SpaceRoleUserEntity, UUID, UUID> twinFieldSpaceUserKit;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private KitGrouped<TwinFieldTwinClassEntity, UUID, UUID> twinFieldTwinClassKit;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private KitGrouped<TwinFieldAttributeEntity, UUID, UUID> twinFieldAttributeKit;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Kit<TwinFieldDecimalEntity, UUID> twinFieldDecimalKit;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Map<UUID, BigDecimal> twinFieldCalculated;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Map<UUID, Boolean> twinFieldEditability;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Map<UUID, Boolean> twinFieldViewability;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FieldRulesApplyResult fieldRulesApplyResult;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Kit<FieldValue, UUID> fieldValuesKit;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinLinkService.FindTwinLinksResult twinLinks;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Kit<TwinflowTransitionEntity, UUID> validTransitionsKit;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Kit<TwinAttachmentEntity, UUID> attachmentKit;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Kit<DataListOptionEntity, UUID> twinMarkerKit;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Kit<DataListOptionEntity, UUID> twinTagKit;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Kit<TwinAliasEntity, TwinAliasType> twinAliases;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Kit<TwinEntity, UUID> segments;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    // we use kitGrouped, because during moving twin to other space or even during class change a new alias will be created,
    // but old aliases also should be accessible for correct url processing
    private KitGrouped<TwinAliasEntity, UUID, TwinAliasType> twinAliasesArchive;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<TwinAction> actions;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Map<TwinAction, UUID> actionsRestricted;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Map<TwinAction, ActionRestrictionReasonEntity> actionsRestrictedReasons;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TwinAttachmentsCount twinAttachmentsCount;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Kit<TwinClassEntity, UUID> creatableChildTwinClasses;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Map<String, Boolean> twinValidatorResultCache;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Set<UUID> headTwinsIdSet;

    public boolean isSketch() {
        return SystemIds.TwinStatus.SKETCH.equals(twinStatusId) || twinStatus.getType().equals(StatusType.SKETCH);
    }

    public Set<UUID> getHeadTwinsIdSet() {
        if (null == headTwinsIdSet && null != hierarchyTree) {
            headTwinsIdSet = LTreeUtils.toUuidsSortedSet(hierarchyTree, true);
        }
        return headTwinsIdSet;
    }

    @Override
    public String toString() {
        return logDetailed();
    }

    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twin[" + id + "]";
            case NORMAL ->
                    "twin[id:" + id + ", " + (twinClass == null ? "twinClassId:" + twinClassId : twinClass.logNormal()) + "]";
            default ->
                    "twin[id:" + id + ", " + (twinClass == null ? "twinClassId:" + twinClassId : twinClass.logNormal()) + ", " + (twinStatus == null ? "twinStatusId:" + twinStatusId : twinStatus.logNormal()) + "]";
        };

    }

    public enum BasicField {
        NAME(Fields.name, SystemIds.TwinClassField.Base.NAME, TwinEntity::getName),
        DESCRIPTION(Fields.description, SystemIds.TwinClassField.Base.DESCRIPTION, TwinEntity::getDescription),
        EXTERNAL_ID(Fields.externalId, SystemIds.TwinClassField.Base.EXTERNAL_ID, TwinEntity::getExternalId),
        OWNER_USER_ID(Fields.ownerUserId, SystemIds.TwinClassField.Base.OWNER_USER_ID, TwinEntity::getOwnerUserId),
        FLAVOR_DATA_LIST_OPTION_ID(Fields.flavorDataListOptionId, SystemEntityService.TWIN_CLASS_FIELD_TWIN_FLAVOR_DATA_LIST_OPTION_ID, TwinEntity::getFlavorDataListOptionId),
        TWIN_CLASS_ID(Fields.twinClassId, SystemIds.TwinClassField.Base.TWIN_CLASS_ID, TwinEntity::getTwinClassId),
        ASSIGNEE_USER_ID(Fields.assignerUserId, SystemIds.TwinClassField.Base.ASSIGNEE_USER_ID, TwinEntity::getAssignerUserId),
        HEAD_TWIN_ID(Fields.headTwinId, SystemIds.TwinClassField.Base.HEAD_ID, TwinEntity::getHeadTwinId),
        CREATOR_USER_ID(Fields.createdByUserId, SystemIds.TwinClassField.Base.CREATOR_USER_ID, TwinEntity::getCreatedByUserId),
        TWIN_STATUS_ID(Fields.twinStatusId, SystemIds.TwinClassField.Base.STATUS_ID, TwinEntity::getTwinStatusId),
        CREATED_AT(Fields.createdAt, SystemIds.TwinClassField.Base.CREATED_AT, TwinEntity::getCreatedAt),
        ID(Fields.id, SystemIds.TwinClassField.Base.ID, TwinEntity::getId),
        ALIAS_SPACE_ID(Fields.aliasSpaceId, SystemIds.TwinClassField.Base.ALIASES, TwinEntity::getAliasSpaceId);

        @Getter
        private final String name;
        @Getter
        private final UUID id;
        private final Function<TwinEntity, Object> functionGetValue;

        BasicField(String name, UUID id, Function<TwinEntity, Object> functionGetValue) {
            this.name = name;
            this.id = id;
            this.functionGetValue = functionGetValue;
        }

        public static BasicField convertOrNull(UUID twinClassFieldId) {
            for (BasicField field : values()) {
                if (field.id.equals(twinClassFieldId))
                    return field;
            }
            return null;
        }

        public Object getValue(TwinEntity twinEntity) {
            return functionGetValue.apply(twinEntity);
        }
    }

    public TwinEntity clone() {
        return new TwinEntity()
                .setId(id)
                .setTwinClassId(twinClassId)
                .setTwinClass(twinClass)
                .setTwinStatusId(twinStatusId)
                .setTwinStatus(twinStatus)
                .setName(name)
                .setAssignerUserId(assignerUserId)
                .setAssignerUser(assignerUser)
                .setCreatedByUserId(createdByUserId)
                .setCreatedByUser(createdByUser)
                .setCreatedAt(createdAt)
                .setHeadTwinId(headTwinId)
                .setHeadTwin(headTwin)
                .setOwnerUserId(ownerUserId)
                .setOwnerBusinessAccountId(ownerBusinessAccountId)
                .setPermissionSchemaSpaceId(permissionSchemaSpaceId)
                .setTwinflowSchemaSpaceId(twinflowSchemaSpaceId)
                .setTwinClassSchemaSpaceId(twinClassSchemaSpaceId)
                .setAliasSpaceId(aliasSpaceId)
                .setViewPermissionId(viewPermissionId)
                .setViewPermissionCustom(viewPermissionCustom)
                .setExternalId(externalId)
                .setDescription(description)
                .setSpaceTwin(spaceTwin);
    }

    public TwinEntity resetTransientState() {
        pageFace = null;
        breadCrumbsFace = null;

        markersLoadState = LoadState.NOT_LOADED;

        spaceTwin = null;
        if (headTwin != null) {
            headTwin.resetTransientState();
            headTwin = null;
        }
        twinflow = null;

        // Kits — core fields
        twinFieldSimpleKit = null;
        twinFieldSimpleNonIndexedKit = null;
        twinFieldI18nKit = null;
        twinFieldBooleanKit = null;
        twinFieldDatalistKit = null;
        twinFieldUserKit = null;
        twinFieldSpaceUserKit = null;
        twinFieldTwinClassKit = null;
        twinFieldAttributeKit = null;
        twinFieldDecimalKit = null;
        twinFieldTimestampKit = null;
        twinFieldEditability = null;
        twinFieldViewability = null;

        // Calculated
        twinFieldCalculated = null;
        fieldValuesKit = null;

        // Links / transitions
        twinLinks = null;
        validTransitionsKit = null;

        // Attachments / markers / tags
        attachmentKit = null;
        twinMarkerKit = null;
        twinTagKit = null;

        // Aliases / segments
        twinAliases = null;
        twinAliasesArchive = null;
        segments = null;

        // Actions / counters
        actions = null;
        actionsRestricted = null;
        actionsRestrictedReasons = null;
        twinAttachmentsCount = null;

        // Permissions / creation helpers
        creatableChildTwinClasses = null;

        // TwinValidators
        twinValidatorResultCache = null;

        // rules
        fieldRulesApplyResult = null;
        return this;
    }

    public TwinEntity resetLoadedFields() {
        if (headTwin != null) {
            headTwin.resetLoadedFields();
        }
        // Kits — core fields
        twinFieldSimpleKit = null;
        twinFieldSimpleNonIndexedKit = null;
        twinFieldI18nKit = null;
        twinFieldBooleanKit = null;
        twinFieldDatalistKit = null;
        twinFieldUserKit = null;
        twinFieldSpaceUserKit = null;
        twinFieldTwinClassKit = null;
        twinFieldAttributeKit = null;
        // Calculated
        twinFieldCalculated = null;
        fieldValuesKit = null;
        // Links
        twinLinks = null;
        // todo
        return this;
    }

    public TwinEntity resetCalculatedFields() {
        if (headTwin != null) {
            headTwin.resetCalculatedFields();
        }
        twinFieldCalculated = null;
        twinFieldAttributeKit = null;
        fieldValuesKit = null;
        return this;
    }
}
