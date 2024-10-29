package org.twins.core.domain.factory;

import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.util.CollectionUtils;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.EntityCUD;
import org.twins.core.domain.TwinBasicFields;
import org.twins.core.domain.TwinUpdate;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

import java.util.*;

@Data
@Accessors(chain = true)
public class FactoryContext {
    private Collection<TwinEntity> inputTwinList;
    private Map<UUID, FieldValue> fields; // key: twinClassFieldId
    private List<FactoryItem> factoryItemList = new ArrayList<>();
    private TwinBasicFields basics = null;
    private FactoryBranchId rootFactoryBranchId;
    private FactoryBranchId currentFactoryBranchId;

    public FactoryContext(FactoryBranchId rootFactoryBranchId) {
        this.rootFactoryBranchId = rootFactoryBranchId;
    }

    private EntityCUD<TwinAttachmentEntity> attachmentCUD;

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

    public List<FactoryItem> getFactoryItemList() {
        return factoryItemList.stream().filter(fi -> fi.getFactoryBranchId().accessibleFrom(currentFactoryBranchId)).toList();
    }

    public List<FactoryItem> getAllFactoryItemList() {
        return factoryItemList;
    }

    private void addFactoryItem(TwinEntity inputTwin) { // inputTwins can be updated in pipelines, so we have to wrap them to FactoryItem
        TwinUpdate twinUpdate = new TwinUpdate();
        twinUpdate
                .setDbTwinEntity(inputTwin)
                .setTwinEntity(new TwinEntity()
                        .setId(inputTwin.getId())
                        .setTwinClass(inputTwin.getTwinClass())
                        .setTwinClassId(inputTwin.getTwinClassId()));
        FactoryItem factoryItem = new FactoryItem()
                .setFactoryContext(this)
                .setOutput(twinUpdate);
        FactoryItem rootItem = new FactoryItem()
                .setFactoryContext(this)
                .setOutput(twinUpdate);
        // we have to do so, because all data for items can be looked up only from context
        // see TwinFactoryService.lookupFieldValue
        factoryItem.setContextFactoryItemList(List.of(rootItem));
        add(factoryItem);
    }

    public void add(FactoryItem factoryItem) {
        factoryItemList.add(factoryItem.setFactoryBranchId(currentFactoryBranchId != null ? currentFactoryBranchId : rootFactoryBranchId));
    }


    public Map<UUID, FieldValue> getFields() {
        if (fields == null)
            fields = new HashMap<>(); //to be sure that no one set in to null
        return fields;
    }

    @Override
    public String toString() {
        return "FactoryContext[" + currentFactoryBranchId + "]";
    }

    public void addAll(List<FactoryItem> multiplierOutput) {
        for (FactoryItem factoryItem : multiplierOutput) {
            add(factoryItem);
        }
    }

    public void currentFactoryBranchLevelUp() {
        currentFactoryBranchId = currentFactoryBranchId.previous();
    }
}
