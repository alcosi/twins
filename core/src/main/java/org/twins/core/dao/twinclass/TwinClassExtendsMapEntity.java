package org.twins.core.dao.twinclass;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;

import java.io.Serializable;
import java.util.UUID;

/**
 * This table is used only for speed up loading inheritance
 */
@Entity
@Data
@Accessors(chain = true)
@Table(name = "twin_class_extends_map")
@IdClass(TwinClassExtendsMapEntity.PK.class)
public class TwinClassExtendsMapEntity implements EasyLoggable {
    @Id
    @Column(name = "twin_class_id")
    private UUID twinClassId;

    @Id
    @Column(name = "extends_twin_class_id")
    private UUID extendsTwinClassId;


    public String easyLog(Level level) {
        return "twinClassExtendsMap[twinClassId:" + twinClassId + ", extendsTwinClassId:" + extendsTwinClassId + "]";
    }

    @Data
    public static class PK implements Serializable {
        @Column(name = "twin_class_id")
        private UUID twinClassId;
        @Column(name = "extends_twin_class_id")
        private UUID extendsTwinClassId;

    }
}
