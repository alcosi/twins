package org.twins.core.dao.factory;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Entity
@Table(name = "twin_factory_eraser_step")
@Accessors(chain = true)
@Data
public class TwinFactoryEraserStepEntity {
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "twin_factory_eraser_id")
    private UUID twinFactoryEraserId;

    @Column(name = "order")
    private Integer order;

    @Column(name = "twin_factory_condition_set_id")
    private UUID twinFactoryConditionSetId;

    @Column(name = "active")
    private boolean active;

    @Column(name = "twin_factory_condition_invert")
    private boolean twinFactoryConditionInvert;

    @Column(name = "description", length = Integer.MAX_VALUE)
    private String description;

    @Column(name = "on_passed_twin_factory_eraser_action_id")
    @Convert(converter = TwinFactoryEraserActionConverter.class)
    private TwinFactoryEraserEntity.Action onPassedTwinFactoryEraserAction;

    @Column(name = "on_failed_twin_factory_eraser_action_id")
    @Convert(converter = TwinFactoryEraserActionConverter.class)
    private TwinFactoryEraserEntity.Action onFailedTwinFactoryEraserAction;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "twin_factory_eraser_id", insertable = false, updatable = false)
    private TwinFactoryEraserEntity twinFactoryEraser;
}