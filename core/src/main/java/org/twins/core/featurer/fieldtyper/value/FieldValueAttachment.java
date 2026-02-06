package org.twins.core.featurer.fieldtyper.value;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.Objects;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueAttachment extends FieldValueStated {
    private String name;
    private String base64Content;

    public FieldValueAttachment(TwinClassFieldEntity twinClassField) {
        super(twinClassField);
    }

    @Override
    public FieldValueAttachment clone(TwinClassFieldEntity newTwinClassFieldEntity) {
        FieldValueAttachment clone = new FieldValueAttachment(newTwinClassFieldEntity);
        clone.setName(name);
        clone.setBase64Content(base64Content);
        return clone;
    }

    @Override
    public boolean hasValue(String value) {
        return Objects.equals(base64Content, value);
    }

    @Override
    public void copyValueFrom(FieldValue src) {
        name = ((FieldValueAttachment) src).getName();
        base64Content = ((FieldValueAttachment) src).getBase64Content();
    }

    @Override
    public void onUndefine() {
        name = null;
        base64Content = null;
    }

    @Override
    public void onClear() {
        name = null;
        base64Content = null;
    }
}