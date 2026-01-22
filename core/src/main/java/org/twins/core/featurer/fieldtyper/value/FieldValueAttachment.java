package org.twins.core.featurer.fieldtyper.value;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueAttachment extends FieldValue {
    private String name;
    private String base64Content;

    public FieldValueAttachment(TwinClassFieldEntity twinClassField) {
        super(twinClassField);
    }

    @Override
    public boolean isFilled() {
        return name != null && !name.isEmpty() && base64Content != null && !base64Content.isEmpty();
    }

    @Override
    public FieldValue clone(TwinClassFieldEntity newTwinClassFieldEntity) {
        FieldValueAttachment clone = new FieldValueAttachment(newTwinClassFieldEntity);
        clone.setName(name);
        clone.setBase64Content(base64Content);
        return clone;
    }

    @Override
    public boolean hasValue(String value) {
        return false;
    }

    @Override
    public void copyValueFrom(FieldValue src) {

    }

    @Override
    public void nullify() {
        name = null;
        base64Content = null;
    }

    @Override
    public boolean isNullified() {
        return name == null && base64Content == null;
    }
}