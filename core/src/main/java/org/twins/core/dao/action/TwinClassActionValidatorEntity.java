package org.twins.core.dao.action;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import org.cambium.common.EasyLoggable;
import org.cambium.featurer.annotations.FeaturerList;
import org.cambium.featurer.dao.FeaturerEntity;
import org.hibernate.annotations.Type;
import org.twins.core.featurer.twin.validator.TwinValidator;

import java.util.HashMap;
import java.util.UUID;

@Entity
@Data
@Table(name = "twin_class_action_validator")
public class TwinClassActionValidatorEntity implements EasyLoggable {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "twin_class_id")
    private UUID twinClassId;

    @Column(name = "twin_action_id")
    @Enumerated(EnumType.STRING)
    private TwinAction twinAction;

    @Column(name = "`order`")
    @Basic
    private Integer order;

    @Column(name = "invert")
    private boolean invert;

    @Column(name = "twin_validator_featurer_id")
    private Integer twinValidatorFeaturerId;

    @FeaturerList(type = TwinValidator.class)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "twin_validator_featurer_id", insertable = false, updatable = false)
    private FeaturerEntity twinValidatorFeaturer;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "twin_validator_params", columnDefinition = "hstore")
    private HashMap<String, String> twinValidatorParams;

    @Column(name = "active")
    private boolean isActive;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinClassActionValidator[" + id + "]";
            case NORMAL -> "twinClassActionValidator[id:" + id + ", twinClassId:" + twinClassId + "]";
            default ->
                    "twinClassActionValidator[id:" + id + ", twinClassId:" + twinClassId + ", twinValidatorFeaturerId:" + twinValidatorFeaturerId + "]";
        };
    }
}
