package org.twins.core.dao.draft;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.user.UserEntity;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Entity
@Table(name = "draft_twin_persist")
public class DraftTwinPersistEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    @Column(name = "id")
    private UUID id;

    @Column(name = "draft_id")
    private UUID draftId;

    @Column(name = "time_in_millis")
    private long timeInMillis;

    @Column(name = "create_else_update")
    private boolean createElseUpdate = false;

    @Column(name = "twin_id")
    private UUID twinId;

    @Column(name = "twin_class_id")
    private UUID twinClassId;

    @Column(name = "head_twin_id")
    private UUID headTwinId;

    @Column(name = "external_id")
    private String externalId;

    @Column(name = "twin_status_id")
    private UUID twinStatusId;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "created_by_user_id")
    private UUID createdByUserId;

    @Column(name = "assigner_user_id")
    private UUID assignerUserId;

    @Column(name = "owner_business_account_id")
    private UUID ownerBusinessAccountId;

    @Column(name = "owner_user_id")
    private UUID ownerUserId;

    //we can not create @ManyToOne relation, because we can have nullify marker here
    @Column(name = "view_permission_id")
    private UUID viewPermissionId;

    @Column(name = "conflict_description")
    private String conflictDescription;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "draft_id", insertable = false, updatable = false)
    private DraftEntity draft;

    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "twin_class_id", insertable = false, updatable = false)
    private TwinClassEntity twinClass;

    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "twin_status_id", insertable = false, updatable = false)
    private TwinStatusEntity twinStatus;

    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "assigner_user_id", insertable = false, updatable = false)
    private UserEntity assigneeUser;

    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "created_by_user_id", insertable = false, updatable = false)
    private UserEntity createdByUser;
}