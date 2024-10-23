package org.twins.core.dao.twin;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import org.cambium.common.EasyLoggable;
import org.cambium.featurer.annotations.FeaturerList;
import org.cambium.featurer.dao.FeaturerEntity;
import org.hibernate.annotations.Type;
import org.twins.core.dao.comment.TwinCommentAction;
import org.twins.core.featurer.twin.validator.TwinValidator;

import java.util.HashMap;
import java.util.UUID;

@Data
@Entity
@Table(name = "twin_validator_set")
public class TwinValidatorSetEntity implements EasyLoggable {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinValidatorSetEntity[" + id + "]";
            case NORMAL -> "twinValidatorSetEntity[id:" + id + ", domainId:" + domainId + "]";
            default ->
                    "twinValidatorSetEntity[id:" + id + ", domainId:" + domainId + ", name:" + name + "]";
        };
    }

}
