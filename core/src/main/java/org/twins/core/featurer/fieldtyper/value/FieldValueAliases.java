package org.twins.core.featurer.fieldtyper.value;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.cambium.common.util.CollectionUtils;
import org.twins.core.dao.twin.TwinAliasEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueAliases extends FieldValue {
    private List<TwinAliasEntity> aliases = new ArrayList<>();

    public FieldValueAliases(TwinClassFieldEntity twinClassField) {
        super(twinClassField);
    }

    @Override
    public boolean isFilled() {
        return CollectionUtils.isNotEmpty(aliases);
    }

    public FieldValueAliases add(TwinAliasEntity alias) {
        aliases.add(alias);
        return this;
    }

    @Override
    public FieldValue clone(TwinClassFieldEntity newTwinClassFieldEntity) {
        FieldValueAliases clone = new FieldValueAliases(newTwinClassFieldEntity);
        clone.getAliases().addAll(this.aliases);
        return clone;
    }

    @Override
    public boolean hasValue(String value) {
        if (CollectionUtils.isEmpty(aliases)) {
            return false;
        }
        UUID valueUUID;
        try {
            valueUUID = UUID.fromString(value);
        } catch (Exception e) {
            return false;
        }
        for (TwinAliasEntity alias : aliases) {
            if (alias.getId() != null && alias.getId().equals(valueUUID))
                return true;
        }
        return false;
    }

    @Override
    public void copyValueFrom(FieldValue src) {
        aliases.clear();
        aliases.addAll(((FieldValueAliases)src).aliases);
    }

    public void nullify() {
        aliases = Collections.EMPTY_LIST;
    }

    @Override
    public boolean isNullified() {
        return aliases != null && aliases.isEmpty();
    }

}
