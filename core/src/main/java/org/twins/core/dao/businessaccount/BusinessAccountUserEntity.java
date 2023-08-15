package org.twins.core.dao.businessaccount;

import lombok.Data;

import jakarta.persistence.*;
import org.twins.core.dao.user.UserEntity;

import java.util.UUID;

@Entity
@Data
@Table(name = "business_account_user")
public class BusinessAccountUserEntity {
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
}
