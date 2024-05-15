package org.twins.core.dao.factory;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;

import java.util.UUID;

@Entity
@Table(name = "twin_factory")
@Accessors(chain = true)
@Data
public class TwinFactoryEntity implements EasyLoggable {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "key")
    private String key;

    @Column(name = "domain_id")
    private UUID domainId;

    @Column(name = "name_i18n_id")
    private UUID nameI18NId;

    @Column(name = "description_i18n_id")
    private UUID descriptionI18NId;

    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinFactory[" + id + "]";
            default -> "twinFactory[id:" + id + ", key:" + key + "]";
        };
    }

}
