package org.twins.core.dao.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.user.UserEntity;

import java.util.UUID;

@Entity
@Table(name = "domain_version_ghost")
@Data
@FieldNameConstants
@Accessors(chain = true)
@IdClass(DomainVersionGhostId.class)
public class DomainVersionGhostEntity implements EasyLoggable {
    @Id
    @Column(name = "domain_id")
    private UUID domainId;

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Id
    @Column(name = "table_name")
    private String tableName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private DomainEntity domain;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private UserEntity user;

    public String easyLog(Level level) {
        return "domainVersionGhost[domainId:" + domainId + ", userId:" + userId + ", table:" + tableName + "]";
    }
}
