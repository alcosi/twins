package org.twins.core.dao.twin;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.twins.core.dao.i18n.LocaleConverter;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.Locale;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@FieldNameConstants
@Table(name = "twin_field_i18n")
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class TwinFieldI18nEntity extends TwinFieldBaseEntity {
    @Column(name = "locale")
    @Convert(converter = LocaleConverter.class)
    private Locale locale;

    @Basic
    @Column(name = "translation", length = 255)
    private String translation;

    @Override
    public TwinFieldI18nEntity setId(UUID id) {
        super.setId(id);
        return this;
    }

    @Override
    public TwinFieldI18nEntity setTwinId(UUID twinId) {
        super.setTwinId(twinId);
        return this;
    }

    @Override
    public TwinFieldI18nEntity setTwinClassFieldId(UUID twinClassFieldId) {
        super.setTwinClassFieldId(twinClassFieldId);
        return this;
    }

    @Override
    public TwinFieldI18nEntity setTwin(TwinEntity twin) {
        super.setTwin(twin);
        return this;
    }

    @Override
    public TwinFieldI18nEntity setTwinClassField(TwinClassFieldEntity twinClassField) {
        super.setTwinClassField(twinClassField);
        return this;
    }

    @Override
    public String easyLog(EasyLoggable.Level level) {
        return "twinFieldI18n[id:" + getId() + "]";
    }

    public TwinFieldI18nEntity cloneFor(TwinEntity dstTwinEntity) {
        return new TwinFieldI18nEntity()
                .setTwin(dstTwinEntity)
                .setTwinId(dstTwinEntity.getId())
                .setTwinClassFieldId(getTwinClassFieldId())
                .setTwinClassField(getTwinClassField())
                .setLocale(locale)
                .setTranslation(translation);
    }
}
