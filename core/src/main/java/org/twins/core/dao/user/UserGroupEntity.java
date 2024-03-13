package org.twins.core.dao.user;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.businessaccount.BusinessAccountEntity;
import org.twins.core.dao.domain.DomainEntity;

import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "user_group")
@FieldNameConstants
public class UserGroupEntity implements EasyLoggable {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "business_account_id")
    private UUID businessAccountId;

    @Column(name = "user_group_type_id")
    private String userGroupTypeId;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @ManyToOne
    @JoinColumn(name = "domain_id", insertable = false, updatable = false)
    private DomainEntity domain;

    @ManyToOne
    @JoinColumn(name = "business_account_id", insertable = false, updatable = false)
    private BusinessAccountEntity businessAccount;

    @ManyToOne
    @JoinColumn(name = "user_group_type_id", insertable = false, updatable = false)
    private UserGroupTypeEntity userGroupType;

    public String easyLog(Level level)  {
        return "userGroup[id:" + id + ", name:" + name + "]";
    }



}
