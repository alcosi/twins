package org.twins.core.dao.validator;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.UuidUtils;
import org.hibernate.annotations.Type;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.Identifiable;

import java.util.HashMap;
import java.util.UUID;

@Entity
@Data
@Table(name = "twin_class_field_validator")
@Accessors(chain = true)
public class TwinClassFieldValidatorEntity implements EasyLoggable, Identifiable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "twin_class_field_id", nullable = false)
    private UUID twinClassFieldId;

    @Column(name = "field_validator_featurer_id", nullable = false)
    private Integer fieldValidatorFeaturerId;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "field_validator_params", columnDefinition = "hstore")
    private HashMap<String, String> fieldValidatorParams;

    @Column(name = "be_validation_error_i18n_id")
    private UUID beValidationErrorI18nId;

    @Transient
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private TwinClassFieldEntity twinClassField;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinClassFieldValidator[" + id + "]";
            case NORMAL -> "twinClassFieldValidator[id:" + id + ", twinClassFieldId:" + twinClassFieldId + "]";
            default -> "twinClassFieldValidator[id:" + id
                    + ", twinClassFieldId:" + twinClassFieldId
                    + ", fieldValidatorFeaturerId:" + fieldValidatorFeaturerId
                    + ", fieldValidatorParams:" + fieldValidatorParams + "]";
        };
    }
}
