package org.twins.core.dao.twin;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.user.UserEntity;

import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "twin_field_user")
public class TwinFieldUserEntity implements EasyLoggable, TwinFieldStorage {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "twin_field_id")
    private UUID twinFieldId;

    @Column(name = "user_id")
    private UUID userId;

    @ManyToOne
    @JoinColumn(name = "twin_field_id", insertable = false, updatable = false, nullable = false)
    private TwinFieldEntity twinField;

    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false, nullable = false)
    private UserEntity user;

    @Override
    public String easyLog(Level level) {
        return "twinFieldUser[id:" + id + "]";
    }
}
