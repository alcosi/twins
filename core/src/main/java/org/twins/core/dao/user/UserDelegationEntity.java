package org.twins.core.dao.user;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.UuidUtils;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "user_delegation")
@FieldNameConstants
public class UserDelegationEntity implements EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "machine_user_id")
    private UUID machineUserId;

    @Column(name = "delegated_user_id")
    private UUID delegatedUserId;

    @Column(name = "added_at")
    private Timestamp addedAt;

    @Column(name = "added_by_user_id")
    private UUID addedByUserId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "added_by_user_id", insertable = false, updatable = false)
    private UserEntity addedByUser;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "delegated_user_id", insertable = false, updatable = false)
    private UserEntity delegatedUser;

    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "userDelegation[id:" + id + "]";
            default ->
                    "userDelegation[id:" + id + ", delegatedUserId:" + delegatedUserId + ", machineUserId:" + machineUserId + ", domainId:" + domainId + "]";
        };
    }
}
