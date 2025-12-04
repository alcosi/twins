package org.twins.core.dao.twin;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;
import org.hibernate.annotations.Type;
import org.twins.core.dao.LtreeUserType;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Accessors(chain = true)
@Data
@Table(name = "twin_archive")
public class TwinArchiveEntity implements Cloneable, EasyLoggable {

    @Id
    private UUID id;

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

    @Column(name = "twin_status_id")
    private UUID twinStatusId;

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> STR."twinArchive[\{id}]";
            case NORMAL -> STR."twinArchive[id:\{id}, twinClassId:\{twinClassId}]";
            default -> STR."twin[id:\{id}, twinClassId:\{twinClassId}, twinStatusId:\{twinStatusId}]";
        };
    }

    public TwinArchiveEntity clone() {
        return new TwinArchiveEntity()
                .setId(id)
                .setTwinClassId(twinClassId)
                .setHeadTwinId(headTwinId)
                .setHierarchyTree(hierarchyTree)
                .setExternalId(externalId)
                .setOwnerBusinessAccountId(ownerBusinessAccountId)
                .setOwnerUserId(ownerUserId)
                .setTwinStatusId(twinStatusId)
                .setCreatedByUserId(createdByUserId)
                .setCreatedAt(createdAt);
    }
}
