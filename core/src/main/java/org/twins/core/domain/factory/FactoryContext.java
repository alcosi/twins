package org.twins.core.domain.factory;

import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.util.CollectionUtils;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.TwinUpdate;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

import java.util.*;

@Data
@Accessors(chain = true)
public class FactoryContext {
    private Collection<TwinEntity> inputTwinList;
    private Map<UUID, FieldValue> fields; // key: twinClassFieldId
    private List<FactoryItem> factoryItemList = new ArrayList<>();

    public FactoryContext addInputTwin(TwinEntity twinEntity) {
        inputTwinList = CollectionUtils.safeAdd(inputTwinList, twinEntity);
        addFactoryItem(twinEntity);
        return this;
    }

    public FactoryContext addInputTwin(Collection<TwinEntity> twinEntityList) {
        inputTwinList = CollectionUtils.safeAdd(inputTwinList, twinEntityList);
        for (TwinEntity twinEntity : twinEntityList)
            addFactoryItem(twinEntity);
        return this;
    }

    public FactoryContext setInputTwinList(Collection<TwinEntity> inputTwinList) {
        this.inputTwinList = inputTwinList;
        for (TwinEntity twinEntity : inputTwinList)
            addFactoryItem(twinEntity);
        return this;
    }

    private void addFactoryItem(TwinEntity inputTwin) { // inputTwins can be updated in pipelines, so we have to wrap them to FactoryItem
        FactoryItem factoryItem = new FactoryItem();
        factoryItem
                .setFactoryContext(this)
                .setOutputTwin(new TwinUpdate()
                        .setDbTwinEntity(inputTwin)
                        .setTwinEntity(new TwinEntity()
                                .setId(inputTwin.getId())
                                .setTwinClass(inputTwin.getTwinClass())
                                .setTwinClassId(inputTwin.getTwinClassId())
                        ));
        factoryItemList.add(factoryItem);
    }
}
