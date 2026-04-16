package org.twins.core.dao.validator;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.UuidUtils;

import java.util.UUID;

@Data
@Entity
@Table(name = "twin_validator_set")
@Accessors(chain = true)
@FieldNameConstants
public class TwinValidatorSetEntity implements EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "invert")
    private Boolean invert;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinValidatorSetEntity[" + id + "]";
            case NORMAL -> "twinValidatorSetEntity[id:" + id + ", domainId:" + domainId + "]";
            default -> "twinValidatorSetEntity[id:" + id + ", domainId:" + domainId + ", name:" + name + "]";
        };
    }

}
