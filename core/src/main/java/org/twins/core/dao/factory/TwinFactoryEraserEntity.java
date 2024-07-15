package org.twins.core.dao.factory;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;

import java.util.Arrays;
import java.util.UUID;

@Entity
@Table(name = "twin_factory_eraser")
@Accessors(chain = true)
@Data
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
    private boolean twinFactoryConditionInvert;

    @Column(name = "active")
    private boolean active;

    @Column(name = "description")
    private String description;

    @Column(name = "final_twin_factory_eraser_action_id")
    @Convert(converter = TwinFactoryEraserActionConverter.class)
    private Action action;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinFactoryEraserEntity[" + id + "]";
            default -> "twinFactoryEraserEntity[id:" + id + ", twinFactoryId:" + twinFactoryId + ", inputTwinClassId:" + inputTwinClassId + "]";
        };
    }

    @Getter
    public enum Action {
        NEXT("NEXT"),
        RESTRICT("RESTRICT"),
        ERASE("ERASE"),
        SKIP("SKIP");

        private final String id;

        Action(String id) {
            this.id = id;
        }

        public static Action valueOd(String type) {
            return Arrays.stream(values()).filter(t -> t.id.equals(type)).findAny().orElse(RESTRICT);
        }

    }
}