package org.twins.core.dao.twin;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.hibernate.annotations.Type;
import org.twins.core.dao.LtreeUserType;
import org.twins.core.enums.action.TwinAction;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dao.businessaccount.BusinessAccountUserEntity;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.domain.DomainBusinessAccountEntity;
import org.twins.core.dao.domain.DomainUserEntity;
import org.twins.core.dao.face.FaceEntity;
import org.twins.core.dao.space.SpaceRoleUserEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinflow.TwinflowEntity;
import org.twins.core.dao.twinflow.TwinflowTransitionEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.TwinAttachmentsCount;
import org.twins.core.enums.twin.TwinAliasType;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.service.SystemEntityService;
import org.twins.core.service.link.TwinLinkService;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Entity
@Accessors(chain = true)
@Data
@Table(name = "twin")
@FieldNameConstants
public class TwinEntity implements Cloneable, EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            this.id = UUID.randomUUID();
        }
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

    @Column(name = "permission_schema_space_id")
    private UUID permissionSchemaSpaceId;

    @Column(name = "twinflow_schema_space_id")
    private UUID twinflowSchemaSpaceId;

    @Column(name = "twin_class_schema_space_id")
    private UUID twinClassSchemaSpaceId;

    @Column(name = "alias_space_id")
    private UUID aliasSpaceId;

    @Column(name = "twin_status_id")
    private UUID twinStatusId;

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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "twin_class_id", referencedColumnName = "id", insertable = false, updatable = false, nullable = false)
    @EqualsAndHashCode.Exclude
    private TwinClassEntity twinClass;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FaceEntity pageFace;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private FaceEntity breadCrumbsFace;

//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "head_twin_id", referencedColumnName = "id", insertable = false, updatable = false, nullable = true)
//    private TwinEntity headTwin;

//    @ManyToOne
//    @JoinColumn(name = "owner_business_account_id", insertable = false, updatable = false)
//    private BusinessAccountEntity ownerBusinessAccount;

    @ManyToOne
    @JoinColumn(name = "twin_status_id", insertable = false, updatable = false, nullable = false)
    @EqualsAndHashCode.Exclude
    private TwinStatusEntity twinStatus;

    @ManyToOne
    @JoinColumn(name = "created_by_user_id", insertable = false, updatable = false, nullable = false)
    private UserEntity createdByUser;

    @ManyToOne
    @JoinColumn(name = "assigner_user_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    private UserEntity assignerUser;

    @ManyToOne
    @JoinColumn(name = "owner_user_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    private UserEntity ownerUser;


    //needed for specification
//    @Deprecated
//    @OneToMany(fetch = FetchType.LAZY)
//    @JoinColumn(name = "id", referencedColumnName = "head_twin_id", insertable = false, updatable = false)
//    @EqualsAndHashCode.Exclude
//    private Collection<TwinEntity> childrenTwins;

    //needed for specification
    @Deprecated
    @OneToMany
    @JoinColumn(name = "twin_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    private Collection<TwinTagEntity> tags;

    //needed for specification
    @Deprecated
    @OneToMany
    @JoinColumn(name = "twin_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    private Collection<TwinMarkerEntity> markers;

    //needed for specification
    @Deprecated
    @OneToMany
    @JoinColumn(name = "src_twin_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    private Collection<TwinLinkEntity> linksBySrcTwinId;

    //needed for specification
    @Deprecated
    @OneToMany
    @JoinColumn(name = "dst_twin_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    private Collection<TwinLinkEntity> linksByDstTwinId;

    //needed for specification
    @Deprecated
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    private Collection<TwinFieldSimpleEntity> fieldsSimple;

    //needed for specification
    @Deprecated
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    private Collection<SpaceRoleUserEntity> spaceRoleUsers;

    //needed for specification
    @Deprecated
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    private Collection<TwinFieldBooleanEntity> fieldsBoolean;

    //needed for specification
    @Deprecated
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    private Collection<TwinFieldDataListEntity> fieldsList;

    //needed for specification
    @Deprecated
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    private Collection<TwinFieldUserEntity> fieldsUser;

    //needed for specification
    @Deprecated
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    private Collection<TwinFieldTwinClassEntity> fieldsTwinClassList;

    //needed for specification
    @Deprecated
    @OneToMany
    @JoinColumn(name = "twin_id", insertable = false, updatable = false)
    private Collection<TwinTouchEntity> touches;

    //needed for specification (USER & BA twins)
    @Deprecated
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    private Set<DomainUserEntity> domainUsers;

    //needed for specification (USER & BA twins)
    @Deprecated
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    private Set<BusinessAccountUserEntity> businessAccountUsersUserTwins;

    //needed for specification (USER & BA twins)
    @Deprecated
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_account_id", referencedColumnName = "id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    private Set<BusinessAccountUserEntity> businessAccountUsersBusinessAccountTwins;

    //needed for specification (USER & BA twins)
    @Deprecated
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_account_id", referencedColumnName = "id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    private Set<DomainBusinessAccountEntity> domainBusinessAccounts;

    @Transient
    @EqualsAndHashCode.Exclude
    private TwinEntity spaceTwin;

    @Transient
    @EqualsAndHashCode.Exclude
    private TwinEntity headTwin;

    @Transient
    @EqualsAndHashCode.Exclude
    private TwinflowEntity twinflow;

    /*
     we have to use TwinClassFieldId as key, not Id. because of case when we load not missing fields
     */
    @Transient
    @EqualsAndHashCode.Exclude
    private Kit<TwinFieldSimpleEntity, UUID> twinFieldSimpleKit;

    @Transient
    @EqualsAndHashCode.Exclude
    private Kit<TwinFieldSimpleNonIndexedEntity , UUID> twinFieldSimpleNonIndexedKit;

    @Transient
    @EqualsAndHashCode.Exclude
    private KitGrouped<TwinFieldI18nEntity, UUID, UUID> twinFieldI18nKit;

    @Transient
    @EqualsAndHashCode.Exclude
    private Kit<TwinFieldBooleanEntity, UUID> twinFieldBooleanKit;

    /*
     we have to use TwinClassFieldId as key, not id. Also, multiple values supported, that is why kit inside a ki
     */
    @Transient
    @EqualsAndHashCode.Exclude
    private KitGrouped<TwinFieldDataListEntity, UUID, UUID> twinFieldDatalistKit;

    @Transient
    @EqualsAndHashCode.Exclude
    private KitGrouped<TwinFieldUserEntity, UUID, UUID> twinFieldUserKit;

    @Transient
    @EqualsAndHashCode.Exclude
    private KitGrouped<SpaceRoleUserEntity, UUID, UUID> twinFieldSpaceUserKit;

    @Transient
    @EqualsAndHashCode.Exclude
    private KitGrouped<TwinFieldTwinClassEntity, UUID, UUID> twinFieldTwinClassKit;

    @Transient
    @EqualsAndHashCode.Exclude
    private Map<UUID, Object> twinFieldCalculated;

    @Transient
    @EqualsAndHashCode.Exclude
    private Kit<FieldValue, UUID> fieldValuesKit;

    @Transient
    @EqualsAndHashCode.Exclude
    private TwinLinkService.FindTwinLinksResult twinLinks;

    @Transient
    @EqualsAndHashCode.Exclude
    private Kit<TwinflowTransitionEntity, UUID> validTransitionsKit;

    @Transient
    @EqualsAndHashCode.Exclude
    private Kit<TwinAttachmentEntity, UUID> attachmentKit;

    @Transient
    @EqualsAndHashCode.Exclude
    private Kit<DataListOptionEntity, UUID> twinMarkerKit;

    @Transient
    @EqualsAndHashCode.Exclude
    private Kit<DataListOptionEntity, UUID> twinTagKit;

    @Transient
    @EqualsAndHashCode.Exclude
    private Kit<TwinAliasEntity, TwinAliasType> twinAliases;

    @Transient
    @EqualsAndHashCode.Exclude
    // we use kitGrouped, because during moving twin to other space or even during class change a new alias will be created,
    // but old aliases also should be accessible for correct url processing
    private KitGrouped<TwinAliasEntity, UUID, TwinAliasType> twinAliasesArchive;

    @Transient
    @EqualsAndHashCode.Exclude
    private Set<TwinAction> actions;

    @Transient
    @EqualsAndHashCode.Exclude
    private TwinAttachmentsCount twinAttachmentsCount;

    @Transient
    @EqualsAndHashCode.Exclude
    private Kit<TwinClassEntity, UUID> creatableChildTwinClasses;

    public boolean isSketch() {
        return SystemEntityService.TWIN_STATUS_SKETCH.equals(twinStatusId);
    }

    @Override
    public String toString() {
        return logDetailed();
    }

    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twin[" + id + "]";
            case NORMAL -> "twin[id:" + id + ", " + (twinClass == null ? "twinClassId:" + twinClassId : twinClass.logNormal()) + "]";
            default -> "twin[id:" + id + ", " + (twinClass == null ? "twinClassId:" + twinClassId : twinClass.logNormal()) + ", " + (twinStatus == null ? "twinStatusId:" + twinStatusId : twinStatus.logNormal()) + "]";
        };

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
                .setExternalId(externalId)
                .setDescription(description)
                .setSpaceTwin(spaceTwin);
    }
}
