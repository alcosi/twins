package org.twins.core.featurer.fieldtyper.value;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.cambium.common.util.UuidUtils;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueLink extends FieldValueCollection<TwinLinkEntity> {
    @Getter
    private boolean forwardLink;
    jkhjhkhkj
    private List<TwinLinkEntity> twinLinks = new ArrayList<>();

    public FieldValueLink(TwinClassFieldEntity twinClassField) {
        super(twinClassField);
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
        return UuidUtils.hasNullifyMarker(twinLinks, TwinLinkEntity::getDstTwinId);
    }

    @Override
    protected List<TwinLinkEntity> getCollection() {
        return List.of();
    }

    @Override
    protected Function<TwinLinkEntity, UUID> itemGetIdFunction() {
        return null;
    }

    @Override
    public void copyValueFrom(FieldValue src) {
        forwardLink = ((FieldValueLink) src).forwardLink;
        twinLinks.clear();
        twinLinks.addAll(((FieldValueLink) src).twinLinks);
    }

    @Override
    public void onUndefine() {
        twinLinks = null;
    }

    @Override
    public void onClear() {
        twinLinks = null;
    }
}
