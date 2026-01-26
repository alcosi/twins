package org.twins.core.domain.factory;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.EntityCUD;
import org.twins.core.domain.TwinBasicFields;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.enums.factory.FactoryLauncher;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Data
@Accessors(chain = true)
public class FactoryContext {
    // we need to know where from factory was launched
    private FactoryLauncher factoryLauncher;
    private Collection<TwinEntity> inputTwinList;
    private Map<UUID, FieldValue> fields; // key: twinClassFieldId
    private Set<FactoryItem> factoryItemList = new LinkedHashSet<>(); // LinkedHashSet must guarantee the order for creating twins in the hierarchy
    private Map<UUID, FactoryItem> factoryItemsUniq = new Hashtable<>(); // this will help to avoid conflict updates of same twin
    private TwinBasicFields basics = null;
    private FactoryBranchId rootFactoryBranchId;
    private FactoryBranchId currentFactoryBranchId;
    // If some factory was run from previous factory pipeline,
    // we had to limit items scope for this factory with items filtered for pipeline
    private Map<FactoryBranchId, Set<FactoryItem>> pipelineScopes = new HashMap<>();
    Map<UUID, UUID> afterCommitFactories = new HashMap<>();
    private UUID runLimitedByOwnerBusinessAccount; //this will help to protect from multi business_account run

    public FactoryContext(FactoryLauncher factoryLauncher, FactoryBranchId rootFactoryBranchId) {
        this.factoryLauncher = factoryLauncher;
        this.rootFactoryBranchId = rootFactoryBranchId;
    }

    private EntityCUD<TwinAttachmentEntity> attachmentCUD;

    public FactoryContext addInputTwin(TwinEntity twinEntity) throws ServiceException {
        inputTwinList = CollectionUtils.safeAdd(inputTwinList, twinEntity);
        createFactoryInputItemAndAdd(twinEntity);
        return this;
    }

    public FactoryContext setInputTwinList(Collection<TwinEntity> inputTwinList) throws ServiceException {
        this.inputTwinList = inputTwinList;
        for (TwinEntity twinEntity : inputTwinList)
            createFactoryInputItemAndAdd(twinEntity);
        return this;
    }

    public Set<FactoryItem> getFactoryItemList() {
        FactoryBranchId currentPipeline = currentFactoryBranchId.getCurrentPipeline();
        if (pipelineScopes.containsKey(currentPipeline)) // we will use limited scope
            return pipelineScopes.get(currentPipeline);
        else
            return factoryItemList.stream().filter(fi -> fi.getFactoryBranchId().accessibleFrom(currentFactoryBranchId)).collect(Collectors.toSet());
    }

    public Set<FactoryItem> getAllFactoryItemList() {
        return factoryItemList;
    }

    private void createFactoryInputItemAndAdd(TwinEntity inputTwin) throws ServiceException {
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
        factoryItem
                .setContextFactoryItemList(List.of(rootItem))
                .setFactoryInputItem(true);
        add(factoryItem);
    }

    public void add(FactoryItem factoryItem) throws ServiceException {
        if (factoryItem.getOutput() instanceof TwinUpdate twinUpdate && factoryItemsUniq.containsKey(twinUpdate.getTwinEntity().getId())) {
            factoryItem = factoryItemsUniq.get(twinUpdate.getTwinEntity().getId()); // we will use already existed factory item, but not new one
            log.warn("Repeated factory item load. Factory context already has {}. ContextFactoryItemList from new item will be skipped", factoryItem);
        } else {
            factoryItem.setFactoryBranchId(currentFactoryBranchId != null ? currentFactoryBranchId : rootFactoryBranchId);
            factoryItemList.add(factoryItem);
            if (factoryItem.getOutput() instanceof TwinUpdate twinUpdate) {
                if (runLimitedByOwnerBusinessAccount == null) {
                    runLimitedByOwnerBusinessAccount = twinUpdate.getDbTwinEntity().getOwnerBusinessAccountId();
                } else if (!runLimitedByOwnerBusinessAccount.equals(twinUpdate.getDbTwinEntity().getOwnerBusinessAccountId())) {
                    throw new ServiceException(ErrorCodeTwins.FACTORY_INPUT_INCORRECT, "Twins from different business accounts can't be processed in one run");
                }
                factoryItemsUniq.put(twinUpdate.getTwinEntity().getId(), factoryItem);
            } else if (factoryItem.getOutput() instanceof TwinCreate twinCreate && factoryItem.getOutput().getTwinEntity().getId() != null) {
                factoryItemsUniq.put(twinCreate.getTwinEntity().getId(), factoryItem);
            }
        }
        if (!pipelineScopes.isEmpty()) { //if factoryItem was created by multiplier we should also add it to current pipeline limited scope
            FactoryBranchId currentPipeline = currentFactoryBranchId.getCurrentPipeline();
            if (pipelineScopes.containsKey(currentPipeline)) // we will add it to limited scope
                pipelineScopes.get(currentPipeline).add(factoryItem);
        }
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

    public void addAll(List<FactoryItem> multiplierOutput) throws ServiceException {
        for (FactoryItem factoryItem : multiplierOutput) {
            add(factoryItem);
        }
    }

    public void currentFactoryBranchLevelUp() {
        currentFactoryBranchId = currentFactoryBranchId.previous();
    }

    public void currentFactoryBranchLevelDown(UUID id) {
        currentFactoryBranchId = currentFactoryBranchId.next(id);
    }

    public void currentFactoryBranchEnterPipeline(UUID id) {
        currentFactoryBranchId = currentFactoryBranchId.enterPipeline(id);
    }

    public void currentFactoryBranchExitPipeline() {
        currentFactoryBranchId = currentFactoryBranchId.exitPipeline();
    }

    public void snapshotPipelineScope(Set<FactoryItem> pipelineInputList) {
        pipelineScopes.put(currentFactoryBranchId, pipelineInputList);
    }

    // if we move to next pipeline or even to top factory we can evict scope
    public void evictPipelineScope() {
        pipelineScopes.remove(currentFactoryBranchId);
    }

    public FactoryItem getFactoryItem(UUID twinId) {
        if (twinId == null)
            return null;
        // we have to check only updated twins
        return factoryItemsUniq.get(twinId);
    }

    public void addAfterCommitFactories(Collection<FactoryItem> factoryItems, UUID afterCommitFactoryId) throws ServiceException {
        for (var factoryItem : factoryItems) {
            UUID alreadyAdded = afterCommitFactories.get(factoryItem.getOutput().getTwinId());
            if (alreadyAdded != null) {
                if (alreadyAdded.equals(afterCommitFactoryId)) {
                    continue;
                }
                throw new ServiceException(ErrorCodeTwins.FACTORY_INCORRECT,
                        "After commit factory[{}] is already selected for twin[{}]. Other factory[{}] is currently locked", alreadyAdded, factoryItem.getOutput().getTwinId(), afterCommitFactoryId);
            } else if (factoryItem.getOutput().isCanTriggerAfterOperationFactory()) {
                afterCommitFactories.put(factoryItem.getOutput().getTwinId(), afterCommitFactoryId);
            }
        }
    }
}
