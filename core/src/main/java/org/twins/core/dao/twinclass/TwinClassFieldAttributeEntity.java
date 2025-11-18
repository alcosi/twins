package org.twins.core.dao.twinclass;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;

import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@FieldNameConstants
@Table(name = "twin_class_field_attribute")
public class TwinClassFieldAttributeEntity implements EasyLoggable {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "twin_class_id")
    private UUID twinClassId;

    @Column(name = "key")
    private String key;

    @Column(name = "note_msg_i18n_id")
    private UUID noteMsgI18nId;

    @Column(name = "create_permission_id")
    private UUID createPermissionId;

    @Column(name = "delete_permission_id")
    private UUID deletePermissionId;

    //todo validate
    @Column(name = "uniq")
    private Boolean uniq;

    @Override
    public String easyLog(Level level) {
        return "twinClassFieldAttribute[id:" + id + "]";
    }
}
