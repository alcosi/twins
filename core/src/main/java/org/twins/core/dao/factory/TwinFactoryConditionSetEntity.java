package org.twins.core.dao.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Entity
@Accessors(chain = true)
@FieldNameConstants
@Table(name = "twin_factory_condition_set")
public class TwinFactoryConditionSetEntity {
    @GeneratedValue(generator = "uuid")
    @Id
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

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
}
