package org.twins.core.featurer.fieldtyper.value;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinAliasEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueAliases extends FieldValueCollectionImmutable<TwinAliasEntity> {
    private List<TwinAliasEntity> aliases = new ArrayList<>();

    public FieldValueAliases(TwinClassFieldEntity twinClassField) {
        super(twinClassField);
    }

    @Override
    protected List<TwinAliasEntity> getCollection() {
        return aliases;
    }

    @Override
    protected Function<TwinAliasEntity, UUID> itemGetIdFunction() {
        return TwinAliasEntity::getId;
    }


    @Override
    public FieldValue clone(TwinClassFieldEntity newTwinClassFieldEntity) {
        FieldValueAliases clone = new FieldValueAliases(newTwinClassFieldEntity);
        clone.getAliases().addAll(this.aliases);
        return clone;
    }


    @Override
    public void copyValueFrom(FieldValue src) {
        aliases.clear();
        aliases.addAll(((FieldValueAliases)src).aliases);
    }

    @Override
    public void onUndefine() {
        aliases = null;
    }

    @Override
    public void onClear() {
        aliases = null;
    }

}
