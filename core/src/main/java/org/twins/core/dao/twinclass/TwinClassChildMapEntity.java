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
@Table(name = "twin_class_child_map")
@IdClass(TwinClassChildMapEntity.PK.class)
public class TwinClassChildMapEntity implements EasyLoggable {
    @Id
    @Column(name = "twin_class_id")
    private UUID twinClassId;

    @Id
    @Column(name = "child_twin_class_id")
    private UUID childTwinClassId;


    public String easyLog(Level level) {
        return "twinClassChildMap[twinClassId:" + twinClassId + ", extendsTwinClassId:" + childTwinClassId + "]";
    }

    @Data
    public static class PK implements Serializable {
        @Column(name = "twin_class_id")
        private UUID twinClassId;
        @Column(name = "child_twin_class_id")
        private UUID childTwinClassId;

    }
}
