package org.twins.core.dao.twin;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.apache.commons.lang3.builder.HashCodeExclude;
import org.cambium.common.EasyLoggable;
import org.cambium.common.EasyLoggableImpl;
import org.twins.core.dao.businessaccount.BusinessAccountEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.user.UserEntity;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Entity
@Accessors(chain = true)
@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "twin")
@FieldNameConstants
public class TwinEntity extends EasyLoggableImpl {
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

    @Column(name = "external_id")
    private String externalId;

    @Column(name = "owner_business_account_id")
    private UUID ownerBusinessAccountId;

    @Column(name = "owner_user_id")
    private UUID ownerUserId;

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

    @Transient
    @HashCodeExclude
    private TwinEntity spaceTwin;

    @Transient
    @HashCodeExclude
    private TwinEntity headTwin;

    @Transient
    @HashCodeExclude
    private List<TwinFieldEntity> twinFieldList;

    public String easyLog(Level level) {
        switch (level) {
            case SHORT:
                return "twin[" + id + "]";
            case NORMAL:
                return "twin[id:" + id + ", twinClassId:" + twinClassId + "]";
            default:
                return "twin[id:" + id + ", twinClassId:" + twinClassId + ", twinStatusId:" + twinStatusId + "]";
        }

    }
}
