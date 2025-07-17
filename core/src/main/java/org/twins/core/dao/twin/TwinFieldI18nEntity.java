package org.twins.core.dao.twin;

import jakarta.persistence.*;
import lombok.Data;
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
public class TwinFieldI18nEntity implements EasyLoggable {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            this.id = UUID.randomUUID();
        }
    }

    @Column(name = "twin_id")
    private UUID twinId;

    @Column(name = "twin_class_field_id")
    private UUID twinClassFieldId;

    @Column(name = "locale")
    @Convert(converter = LocaleConverter.class)
    private Locale locale;

    @Basic
    @Column(name = "translation", length = 255)
    private String translation;

    @ManyToOne
    @JoinColumn(name = "twin_id", insertable = false, updatable = false, nullable = false)
    private TwinEntity twin;

    @ManyToOne
    @JoinColumn(name = "twin_class_field_id", insertable = false, updatable = false, nullable = false)
    private TwinClassFieldEntity twinClassField;

    @Override
    public String easyLog(Level level) {
        return "twinFieldI18n[id:" + id + "]";
    }


    public TwinFieldI18nEntity cloneFor(TwinEntity dstTwinEntity) {
        return new TwinFieldI18nEntity()
                .setTwin(dstTwinEntity)
                .setTwinId(dstTwinEntity.getId())
                .setTwinClassFieldId(twinClassFieldId)
                .setTwinClassField(twinClassField)
                .setLocale(locale)
                .setTranslation(translation);
    }
}



