package org.twins.core.dao.twinclass;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.cambium.common.util.UuidUtils;
import org.twins.core.dao.twin.TwinStatusEntity;

import java.util.UUID;


@Entity
@Data
@Accessors(chain = true)
@FieldNameConstants
@Table(name = "twin_class_freeze")
public class TwinClassFreezeEntity implements EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "key")
    private String key;

    @Column(name = "name_i18n_id")
    private UUID nameI18NId;

    @Column(name = "twin_status_id")
    private UUID twinStatusId;

    @Column(name = "description_i18n_id")
    private UUID descriptionI18NId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "twin_status_id", referencedColumnName = "id", insertable = false, updatable = false, nullable = false)
    private TwinStatusEntity twinStatus;

    public String easyLog(Level level) {
        return switch (level) {
            case SHORT -> "twinClassFreeze[" + key + "]";
            default -> "twinClassFreeze[id:" + id + ", key:" + key + ", statusId:" + twinStatusId + "]";
        };

    }
}
