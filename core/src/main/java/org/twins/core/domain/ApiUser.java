package org.twins.core.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.businessaccount.BusinessAccountEntity;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.user.UserEntity;

@Data
@Accessors(chain = true)
public class ApiUser {
    private DomainEntity domain;
    private UserEntity user;
    private BusinessAccountEntity businessAccount;
    private Channel channel;
}
