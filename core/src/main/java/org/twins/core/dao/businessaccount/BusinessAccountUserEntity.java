package org.twins.core.dao.businessaccount;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.user.UserEntity;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@FieldNameConstants
@Table(name = "business_account_user")
public class BusinessAccountUserEntity implements EasyLoggable {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "business_account_id")
    private UUID businessAccountId;

    @Column(name = "user_id")
    private UUID userId;

    @ManyToOne
    @JoinColumn(name = "business_account_id", insertable = false, updatable = false)
    private BusinessAccountEntity businessAccount;

    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private UserEntity user;

    @Column(name = "created_at")
    private Timestamp createdAt;

    public String easyLog(Level level) {
        return "businessAccountUser[id:" + id + ", businessAccount:" + businessAccountId + ", user:" + userId;
    }
}
