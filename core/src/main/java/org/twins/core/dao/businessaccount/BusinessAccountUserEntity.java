package org.twins.core.dao.businessaccount;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.PrettyLoggable;
import org.twins.core.dao.user.UserEntity;

import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "business_account_user")
public class BusinessAccountUserEntity implements PrettyLoggable {
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

    public String logShort() {
        return "businessAccountUser[id:" + id + ", businessAccount:" + businessAccountId + ", user:" + userId;
    }
}
