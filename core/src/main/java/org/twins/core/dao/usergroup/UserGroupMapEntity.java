package org.twins.core.dao.usergroup;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.UuidUtils;
import org.twins.core.dao.businessaccount.BusinessAccountEntity;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.user.UserGroupEntity;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "user_group_map")
@FieldNameConstants
public class UserGroupMapEntity implements EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "user_group_id")
    private UUID userGroupId;

    @Column(name = "user_group_type_id")
    private String userGroupTypeId;

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "business_account_id")
    private UUID businessAccountId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "involves_count", insertable = false, updatable = false)
    private Integer involvesCount;

    @Column(name = "added_at")
    private Timestamp addedAt;

    @Column(name = "added_manually")
    private Boolean addedManually;

    @Column(name = "added_by_user_id")
    private UUID addedByUserId;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_group_id", insertable = false, updatable = false, nullable = false)
    private UserGroupEntity userGroupSpecOnly;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_id", insertable = false, updatable = false)
    private DomainEntity domainSpecOnly;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false, nullable = false)
    private UserEntity userSpecOnly;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "added_by_user_id", insertable = false, updatable = false)
    private UserEntity addedByUserSpecOnly;

    @Deprecated // for specification only
    @Getter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_account_id", insertable = false, updatable = false)
    private BusinessAccountEntity businessAccountSpecOnly;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private UserGroupEntity userGroup;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private DomainEntity domain;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private UserEntity user;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private UserEntity addedByUser;

    @Transient
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private BusinessAccountEntity businessAccount;

//    @Deprecated //for specification only
//    @EqualsAndHashCode.Exclude
//    @OneToMany(mappedBy = "userGroup", fetch = FetchType.LAZY)
//    private Set<SpaceRoleUserGroupEntity> spaceRoleUserGroups;

    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "userGroupMap[id:" + id + "]";
            default ->
                    "userGroupMap[id:" + id + ", userGroupId:" + userGroupId + ", userId:" + userId + ", domainId:" + domainId + ", businessAccountId:" + businessAccountId + "]";
        };
    }
}
