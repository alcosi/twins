package org.twins.core.dao.twin;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggableImpl;
import org.cambium.common.Kit;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinflow.TwinflowTransitionEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.service.link.TwinLinkService;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.UUID;

@Entity
@Accessors(chain = true)
@Data
@EqualsAndHashCode(callSuper = false)
@Table(name = "twin")
@FieldNameConstants
public class TwinEntity extends EasyLoggableImpl implements Cloneable {
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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "twin_class_id", referencedColumnName = "id", insertable = false, updatable = false, nullable = false)
    private TwinClassEntity twinClass;

//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "head_twin_id", referencedColumnName = "id", insertable = false, updatable = false, nullable = true)
//    private TwinEntity headTwin;

//    @ManyToOne
//    @JoinColumn(name = "owner_business_account_id", insertable = false, updatable = false)
//    private BusinessAccountEntity ownerBusinessAccount;

//    @ManyToOne
//    @JoinColumn(name = "owner_user_id", insertable = false, updatable = false)
//    private UserEntity ownerUser;

    @ManyToOne
    @JoinColumn(name = "twin_status_id", insertable = false, updatable = false, nullable = false)
    private TwinStatusEntity twinStatus;

    @ManyToOne
    @JoinColumn(name = "created_by_user_id", insertable = false, updatable = false, nullable = false)
    private UserEntity createdByUser;

    @ManyToOne
    @JoinColumn(name = "assigner_user_id", insertable = false, updatable = false)
    private UserEntity assignerUser;

    @OneToMany
    @JoinColumn(name = "src_twin_id", insertable = false, updatable = false)
    private Collection<TwinLinkEntity> linksBySrcTwinId;

    @OneToMany
    @JoinColumn(name = "dst_twin_id", insertable = false, updatable = false)
    private Collection<TwinLinkEntity> linksByDstTwinId;


    @Transient
    @EqualsAndHashCode.Exclude
    private TwinEntity spaceTwin;

    @Transient
    @EqualsAndHashCode.Exclude
    private TwinEntity headTwin;

    /*
     we have to use TwinClassFieldId as key, not Id. because of case when we load not missing fields
     */
    @Transient
    @EqualsAndHashCode.Exclude
    private Kit<TwinFieldEntity> twinFieldKit;

    @Transient
    @EqualsAndHashCode.Exclude
    private TwinLinkService.FindTwinLinksResult twinLinks;

    @Transient
    @EqualsAndHashCode.Exclude
    private Kit<TwinflowTransitionEntity> validTransitionsKit;

    @Transient
    @EqualsAndHashCode.Exclude
    private Kit<TwinAttachmentEntity> attachmentKit;

    @Transient
    @EqualsAndHashCode.Exclude
    private Kit<DataListOptionEntity> twinMarkerKit;

    @Transient
    @EqualsAndHashCode.Exclude
    private Kit<DataListOptionEntity> twinTagKit;

    @Override
    public String toString() {
        return logDetailed();
    }

    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "twin[" + id + "]";
            case NORMAL:
                return "twin[id:" + id + ", " + (twinClass == null ? "twinClassId:" + twinClassId : twinClass.logNormal()) + "]";
            default:
                return "twin[id:" + id + ", " + (twinClass == null ? "twinClassId:" + twinClassId : twinClass.logNormal()) + ", " + (twinStatus == null ? "twinStatusId:" + twinStatusId : twinStatus.logNormal()) + "]";
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
                .setCreatedByUserId(createdByUserId)
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
