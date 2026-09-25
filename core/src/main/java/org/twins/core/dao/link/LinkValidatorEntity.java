package org.twins.core.dao.link;

import io.hypersistence.utils.hibernate.type.basic.PostgreSQLHStoreType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.UuidUtils;
import org.hibernate.annotations.Type;

import java.util.HashMap;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "link_validator")
@Accessors(chain = true)
public class LinkValidatorEntity implements EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "link_id", nullable = false)
    private UUID linkId;

    @Column(name = "`order`", nullable = false)
    private Integer order;

    @Column(name = "linker_featurer_id", nullable = false)
    private Integer linkerFeaturerId;

    @Type(PostgreSQLHStoreType.class)
    @Column(name = "linker_params", columnDefinition = "hstore")
    private HashMap<String, String> linkerParams;

    @Override
    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "linkValidator[" + id + "]";
            case NORMAL -> "linkValidator[id:" + id + ", linkId:" + linkId + "]";
            default -> "linkValidator[id:" + id
                    + ", linkId:" + linkId
                    + ", linkerFeaturerId:" + linkerFeaturerId
                    + ", linkerParams:" + linkerParams + "]";
        };
    }
}
