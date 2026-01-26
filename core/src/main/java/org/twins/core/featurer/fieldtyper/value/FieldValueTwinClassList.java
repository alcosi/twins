package org.twins.core.featurer.fieldtyper.value;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueTwinClassList extends FieldValueCollection<TwinClassEntity> {
    @Getter
    private List<TwinClassEntity> twinClasses = null;

    public FieldValueTwinClassList(TwinClassFieldEntity twinClassFieldEntity) {
        super(twinClassFieldEntity);
    }

    @Override
    protected List<TwinClassEntity> getCollection() {
        return twinClasses;
    }

    @Override
    protected Function<TwinClassEntity, UUID> itemGetIdFunction() {
        return TwinClassEntity::getId;
    }

    public FieldValueTwinClassList setTwinClasses(Collection<TwinClassEntity> newTwinClasses) {
        twinClasses = setWithNullifyMarkerSupport(newTwinClasses);
        return this;
    }

    public FieldValueTwinClassList add(TwinClassEntity newTwinClass) {
        twinClasses = addWithNullifyMarkerSupport(twinClasses, newTwinClass);
        return this;
    }

    @Override
    public FieldValue clone(TwinClassFieldEntity newTwinClassFieldEntity) {
        FieldValueTwinClassList clone = new FieldValueTwinClassList(newTwinClassFieldEntity);
        clone.twinClasses = this.twinClasses;
        return clone;
    }

    @Override
    public void copyValueFrom(FieldValue src) {
        twinClasses.clear();
        twinClasses.addAll(((FieldValueTwinClassList) src).twinClasses);
    }

    @Override
    public void onUndefine() {
        twinClasses = null;
    }

    @Override
    public void onClear() {
        twinClasses = null;
    }
}