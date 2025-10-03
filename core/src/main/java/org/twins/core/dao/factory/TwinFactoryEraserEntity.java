package org.twins.core.dao.factory;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.enums.factory.FactoryEraserAction;

import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
@Entity
@Table(name = "twin_factory_eraser")
public class TwinFactoryEraserEntity implements EasyLoggable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "twin_factory_id")
    private UUID twinFactoryId;

    @Column(name = "input_twin_class_id")
    private UUID inputTwinClassId;

    @Column(name = "twin_factory_condition_set_id")
    private UUID twinFactoryConditionSetId;

    @Column(name = "twin_factory_condition_invert")
    private Boolean twinFactoryConditionInvert;

    @Column(name = "active")
    private Boolean active;

    @Column(name = "description")
    private String description;

    @Column(name = "twin_factory_eraser_action")
    @Convert(converter = TwinFactoryEraserActionConverter.class)
    private FactoryEraserAction eraserAction;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_factory_id", insertable = false, updatable = false)
    private TwinFactoryEntity twinFactory;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "twin_factory_condition_set_id", insertable = false, updatable = false)
    private TwinFactoryConditionSetEntity conditionSet;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "input_twin_class_id", insertable = false, updatable = false)
    private TwinClassEntity inputTwinClass;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinFactoryEraser[" + id + "]";
            case NORMAL -> "twinFactoryEraser[" + id + ", twinFactoryId:" + twinFactoryId + "]";
            default -> "twinFactoryEraser[id:" + id + ", twinFactoryId:" + twinFactoryId + ", inputTwinClassId:" + inputTwinClassId + ", action:" + eraserAction + "]";
        };
    }

}
