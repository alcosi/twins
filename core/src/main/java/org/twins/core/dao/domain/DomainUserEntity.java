package org.twins.core.dao.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.user.UserEntity;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "domain_user")
public class DomainUserEntity implements EasyLoggable {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "created_at")
    private Timestamp createdAt;

//    @ManyToOne
//    @JoinColumn(name = "domain_id", insertable = false, updatable = false)
//    private DomainEntity domain;
//
//    @ManyToOne
//    @JoinColumn(name = "user_id", insertable = false, updatable = false)
//    private UserEntity user;

    public String easyLog(Level level) {
        return "domainUser[id:" + id + ", domainId:" + domainId + ", userId:" + userId + "]";
    }
}
