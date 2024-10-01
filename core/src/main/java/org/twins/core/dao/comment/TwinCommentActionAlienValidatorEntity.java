package org.twins.core.dao.comment;

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

@Data
@Entity
@Table(name = "twin_comment_action_alien_validator")
public class TwinCommentActionAlienValidatorEntity implements EasyLoggable {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "twin_class_id")
    private UUID twinClassId;

    @Column(name = "twin_comment_action_id")
    @Enumerated(EnumType.STRING)
    private TwinCommentAction twinCommentAction;

    @Column(name = "`order`")
    private Integer order;

    @Column(name = "twin_validator_featurer_id")
    private Integer twinValidatorFeaturerId;

    @FeaturerList(type = TwinValidator.class)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "twin_validator_featurer_id", insertable = false, updatable = false)
    private FeaturerEntity twinValidatorFeaturer;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "twin_validator_params", columnDefinition = "hstore")
    private HashMap<String, String> twinValidatorParams;

    @Column(name = "invert")
    private boolean invert;

    @Column(name = "active")
    private boolean isActive;

    @Override
    public String easyLog(EasyLoggable.Level level) {
        return switch (level) {
            case SHORT -> "twinCommentActionAlienValidator[" + id + "]";
            case NORMAL -> "twinCommentActionAlienValidator[id:" + id + ", twinClassId:" + twinClassId + "]";
            default ->
                    "twinCommentActionAlienValidator[id:" + id + ", twinClassId:" + twinClassId + ", twinValidatorFeaturerId:" + twinValidatorFeaturerId + "]";
        };
    }

}
