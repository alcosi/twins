package org.twins.core.dao.factory;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;

import java.util.UUID;

@Entity
@Table(name = "twin_factory_branch")
@Accessors(chain = true)
@Data
public class TwinFactoryBranchEntity implements EasyLoggable {
    @GeneratedValue(generator = "uuid")
    @Id
    private UUID id;

    @Column(name = "twin_factory_id")
    private UUID twinFactoryId;

    @Column(name = "twin_factory_condition_set_id")
    private UUID twinFactoryConditionSetId;

    @Column(name = "twin_factory_condition_invert")
    private boolean twinFactoryConditionInvert;

    @Column(name = "active")
    private boolean active;

    @Column(name = "next_twin_factory_id")
    private UUID nextTwinFactoryId;

    @Column(name = "description")
    private String description;

    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinFactoryBranch[" + id + "]";
            default -> "twinFactoryBranch[id:" + id + ", twinFactoryId:" + twinFactoryId + ", twinFactoryConditionSetId:" + twinFactoryConditionSetId + "]";
        };

    }
}
