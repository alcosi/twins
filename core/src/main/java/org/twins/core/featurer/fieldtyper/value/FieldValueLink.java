package org.twins.core.featurer.fieldtyper.value;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.UUID;
import java.util.function.Function;

@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueLink extends FieldValueCollection<TwinLinkEntity> {
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
    protected Function<TwinLinkEntity, UUID> itemGetIdFunction() {
        return TwinLinkEntity::getDstTwinId;
    }

    @Override
    public void copyValueTo(FieldValue dst) {
        var dstValue = (FieldValueLink) dst;
        dstValue.clear();
        for (TwinLinkEntity twinLinkEntity : collection) {
            dstValue.collection.add(twinLinkEntity.clone()); //link and src twin will filled on serialize in FieldTyperLink
        }
        dstValue.forwardLink = forwardLink;
    }
}
