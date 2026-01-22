package org.twins.core.featurer.fieldtyper.value;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.cambium.common.util.CollectionUtils;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueLink extends FieldValue {
    private boolean forwardLink;
    private List<TwinLinkEntity> twinLinks = new ArrayList<>();

    public FieldValueLink(TwinClassFieldEntity twinClassField) {
        super(twinClassField);
    }

    @Override
    public boolean isFilled() {
        return CollectionUtils.isNotEmpty(twinLinks);
    }

    public FieldValueLink add(TwinLinkEntity twinLinkEntity) {
        twinLinks.add(twinLinkEntity);
        return this;
    }

    @Override
    public FieldValue clone(TwinClassFieldEntity newTwinClassFieldEntity) {
        FieldValueLink clone = new FieldValueLink(newTwinClassFieldEntity);
        clone.setForwardLink(this.forwardLink);
        for (TwinLinkEntity twinLinkEntity : twinLinks) {
            clone.getTwinLinks().add(twinLinkEntity.clone()); //link and src twin will filled on serialize in FieldTyperLink
        }
        return clone;
    }

    @Override
    public boolean hasValue(String value) {
        UUID valueUUID;
        try {
            valueUUID = UUID.fromString(value);
        } catch (Exception e) {
            return false;
        }
        for (TwinLinkEntity linkEntity : twinLinks) {
            if (linkEntity.getDstTwinId() != null && linkEntity.getDstTwinId().equals(valueUUID)) // only dst twin id is checking
                return true;
        }
        return false;
    }

    @Override
    public void copyValueFrom(FieldValue src) {
        forwardLink = ((FieldValueLink) src).forwardLink;
        twinLinks.clear();
        twinLinks.addAll(((FieldValueLink) src).twinLinks);
    }

    public void nullify() {
        twinLinks = Collections.EMPTY_LIST;
    }

    @Override
    public boolean isNullified() {
        return twinLinks != null && twinLinks.isEmpty();
    }

}
