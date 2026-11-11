package org.twins.core.featurer.fieldtyper.storage;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.KitGrouped;
import org.cambium.featurer.FeaturerService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.specifications.GroupExpressionProvider;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldDataListEntity;
import org.twins.core.dao.twin.TwinFieldDataListRepository;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.FieldTyperSelect;

import java.util.Collection;
import java.util.Properties;

@Component
@RequiredArgsConstructor
public class TwinFieldStorageDatalist extends TwinFieldStorageMater<TwinFieldDataListEntity> {
    private final TwinFieldDataListRepository twinFieldDataListRepository;
    @Lazy
    private final FeaturerService featurerService;

    @Override
    protected TwinFieldDataListRepository repository() {
        return twinFieldDataListRepository;
    }

    @Override
    protected void assignKit(TwinEntity twin, Collection<TwinFieldDataListEntity> entities) {
        twin.setTwinFieldDatalistKit(new KitGrouped<>(entities, TwinFieldDataListEntity::getId, TwinFieldDataListEntity::getTwinClassFieldId));
    }

    @Override
    public boolean isLoaded(TwinEntity twinEntity) {
        return twinEntity.getTwinFieldDatalistKit() != null;
    }

    @Override
    public void initEmpty(TwinEntity twinEntity) {
        twinEntity.setTwinFieldDatalistKit(new KitGrouped<>(TwinFieldDataListEntity::getId, TwinFieldDataListEntity::getTwinClassFieldId)); //not empty kit, because it's immutable, and we need kit update on field serialization
    }

    @Override
    public GroupExpressionProvider<TwinEntity> countGroup(TwinClassFieldEntity field) throws ServiceException {
        return countGroupByColumn(field.getId(), TwinEntity.Fields.fieldsListSpecOnly, TwinFieldDataListEntity.Fields.dataListOptionId, isMultipleSelect(field));
    }

    /**
     * A multiple select stores one row per chosen option, so the same twin matches the join
     * more than once. Count that twin once inside each option group.
     */
    private boolean isMultipleSelect(TwinClassFieldEntity field) throws ServiceException {
        FieldTyper fieldTyper = featurerService.getFeaturer(field.getFieldTyperFeaturerId(), FieldTyper.class);
        if (!(fieldTyper instanceof FieldTyperSelect)) {
            return false;
        }
        Properties properties = featurerService.extractProperties(fieldTyper, field.getFieldTyperParams());
        return FieldTyperSelect.multiple.extract(properties);
    }
}
