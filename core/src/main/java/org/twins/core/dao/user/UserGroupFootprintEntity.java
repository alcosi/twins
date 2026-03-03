package org.twins.core.dao.user;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.UuidUtils;
import org.hibernate.annotations.Immutable;

import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Immutable
@Table(name = "user_group_footprint")
@FieldNameConstants
public class UserGroupFootprintEntity implements EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    public String easyLog(Level level) {
        return "userGroupFootPrint[id:" + id + "]";
    }
}
