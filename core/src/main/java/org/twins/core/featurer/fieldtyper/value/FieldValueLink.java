package org.twins.core.featurer.fieldtyper.value;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.cambium.common.exception.ServiceException;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.UUID;
import java.util.function.Function;

@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueLink extends FieldValueCollection<TwinEntity> {
    @Getter
    @Setter
    private boolean forwardLink;

    public FieldValueLink(TwinClassFieldEntity twinClassField) {
        super(twinClassField);
    }

    @Override
    public FieldValueLink newInstance(TwinClassFieldEntity newTwinClassFieldEntity) {
        return new FieldValueLink(newTwinClassFieldEntity);
    }

    @Override
    protected Function<TwinEntity, UUID> itemGetIdFunction() {
        return TwinEntity::getId;
    }

    @Override
    public void copyValueTo(FieldValue dst) {
        var dstValue = (FieldValueLink) dst;
        dstValue.forwardLink = forwardLink;
        if (collection == null) { // undefined source -> undefined destination, mirroring FieldValueCollection.copyValueTo
            dstValue.collection = null;
            return;
        }
        dstValue.clear();
        for (TwinEntity twinEntity : collection) {
            dstValue.collection.add(twinEntity.clone()); // the twin_link to this far twin will be built on serialize in the link field typer
        }
    }

    public static TwinEntity getSingleLinkedTwinSafe(FieldValue fieldValue) throws ServiceException {
        TwinEntity linkedTwin = null;
        if (fieldValue instanceof FieldValueLink fieldValueLink) {
            if (fieldValueLink.size() > 1) {
                throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_TYPE_INCORRECT, "TwinClassField[" + fieldValue.getTwinClassFieldId() + "] has " + fieldValueLink.size() + " linked twins");
            }
            linkedTwin = fieldValueLink.getItems().getFirst(); // items carry the far twins
        } else if (fieldValue instanceof FieldValueLinkSingle fieldValueLinkSingle) {
            linkedTwin = fieldValueLinkSingle.getValue();
        } else {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_TYPE_INCORRECT, "TwinClassField[" + fieldValue.getTwinClassFieldId() + "] is not of type link");
        }
        return linkedTwin;
    }

    public static TwinEntity getSingleLinkedTwin(FieldValue fieldValue) throws ServiceException {
        if (fieldValue instanceof FieldValueLinkSingle linkSingle && linkSingle.isNotEmpty()) {
            return linkSingle.getValue();
        }
        if (fieldValue instanceof FieldValueLink link && link.isNotEmpty()) {
            return link.getItems().getFirst(); // items carry the far twins
        }
        return null;
    }
}
