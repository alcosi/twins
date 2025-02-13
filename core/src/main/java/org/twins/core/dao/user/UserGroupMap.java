package org.twins.core.dao.user;

import java.sql.Timestamp;
import java.util.UUID;


public interface UserGroupMap {
    UUID getId();
    UUID getUserId();
    UserGroupEntity getUserGroup();
    UUID getUserGroupId();
    Timestamp getAddedAt();
    UUID getAddedByUserId();
}
