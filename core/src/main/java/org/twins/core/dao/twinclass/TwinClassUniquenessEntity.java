package org.twins.core.dao.twinclass;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.UuidUtils;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@FieldNameConstants
@Table(name = "twin_class_uniqueness")
public class TwinClassUniquenessEntity implements EasyLoggable {

    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "twin_class_id")
    private UUID twinClassId;

    @Column(name = "key")
    private String key;

    @Column(name = "name")
    private String name;

    @Column(name = "inheritable")
    private Boolean inheritable;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "uniqueness[" + id + "]";
            default ->
                    "uniqueness[id:" + id + ", twinClassId:" + twinClassId + ", key:" + key + "]";
        };
    }
}
