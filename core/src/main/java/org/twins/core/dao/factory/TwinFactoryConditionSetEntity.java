package org.twins.core.dao.factory;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;

import java.util.UUID;

@Data
@Entity
@Accessors(chain = true)
@FieldNameConstants
@Table(name = "twin_factory_condition_set")
public class TwinFactoryConditionSetEntity implements EasyLoggable {
    @GeneratedValue(generator = "uuid")
    @Id
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "domain_id")
    private UUID domainId;

    @Transient
    private Integer inFactoryPipelineUsagesCount;

    @Transient
    private Integer inFactoryPipelineStepUsagesCount;

    @Transient
    private Integer inFactoryMultiplierFilterUsagesCount;

    @Transient
    private Integer inFactoryBranchUsagesCount;

    @Transient
    private Integer inFactoryEraserUsagesCount;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinFactoryConditionSet[" + id + "]";
            default -> "twinFactoryConditionSet[id:" + id + ", domainId:" + domainId + "]";
        };
    }
}
