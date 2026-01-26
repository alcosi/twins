package org.twins.core.service.twin;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.featurer.FeaturerService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.twinflow.TwinflowEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.twinoperation.TwinChangeClass;
import org.twins.core.dto.rest.twin.TwinChangeClassStrategy;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorage;
import org.twins.core.service.twinclass.TwinClassFieldService;
import org.twins.core.service.twinclass.TwinClassService;
import org.twins.core.service.twinflow.TwinflowService;

import java.util.*;

@Lazy
@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinUpdateClassService {

    private final TwinService twinService;
    private final TwinClassFieldService twinClassFieldService;
    private final TwinflowService twinflowService;
    private final TwinClassService twinClassService;
    private final TwinHeadService twinHeadService;
    private final TwinStatusService twinStatusService;
    private final FeaturerService featurerService;

    @Transactional
    public void changeClassOfTwin(TwinChangeClass twinChangeClass) throws ServiceException {
        //todo implement(recorder?)
        TwinChangesCollector twinChangesCollector = new TwinChangesCollector();

        // Find the twin entity and check permissions
        TwinEntity dbTwinEntity = twinService.findEntitySafe(twinChangeClass.getTwinId());

        // return if nothing to change
        if (twinChangeClass.getNewTwinClassId().equals(dbTwinEntity.getTwinClassId())) return;

        // get old & new twin classes
        TwinClassEntity newTwinClass = twinClassService.findEntitySafe(twinChangeClass.getNewTwinClassId());
        TwinClassEntity oldTwinClass = dbTwinEntity.getTwinClass();

        changeHead(dbTwinEntity, newTwinClass, twinChangeClass, twinChangesCollector);
        compareAndUpdateTwinFields(dbTwinEntity, oldTwinClass, newTwinClass, twinChangeClass, twinChangesCollector);
        updateStatus(dbTwinEntity, newTwinClass, twinChangeClass, twinChangesCollector);
        //todo links...future support(throw if dbtwin has links dst&src)
        //todo childs...future support(throw if dbtwin has childs by head)

        // Update the twin class
        dbTwinEntity.setTwinClassId(newTwinClass.getId());
        dbTwinEntity.setTwinClass(newTwinClass);

        // Save the updated twin entity
        twinService.saveSafe(dbTwinEntity);
    }

    private void changeHead(TwinEntity dbTwinEntity, TwinClassEntity newTwinClass, TwinChangeClass twinChangeClass, TwinChangesCollector twinChangesCollector) throws ServiceException {
        // todo m.b. use overloaded setHeadSafe(dbTwinEntity);
        if (twinChangeClass.getNewHeadTwinId() != null) dbTwinEntity.setHeadTwinId(twinChangeClass.getNewHeadTwinId());
        // Handle head twin updates based on headTwinClassId
        if (newTwinClass.getHeadTwinClassId() != null && dbTwinEntity.getHeadTwinId() == null) {
            // If the twin hasn't a head but the new class have a headTwinClassId throw an error
            throw new ServiceException(ErrorCodeTwins.HEAD_TWIN_NOT_SPECIFIED, "Twin's head doesn't match the requirements of the new class. The new class supports a head twin.");
        } else if (newTwinClass.getHeadTwinClassId() == null) {
            // If the head of new class is null, set the head of twin to null
            dbTwinEntity.setHeadTwinId(null);
        } else if (newTwinClass.getHeadTwinClassId() != null) {
            // If the new class has a headTwinClassId, update the head twin
            // We need to check if the head twin is valid for the new class
            twinHeadService.checkValidHeadForClass(dbTwinEntity.getHeadTwinId(), newTwinClass);
        }

        // update permission schema for twin
        if (dbTwinEntity.getHeadTwinId() != null) {
            TwinEntity headTwin = twinService.findEntitySafe(dbTwinEntity.getHeadTwinId());
            dbTwinEntity.setPermissionSchemaSpaceId(twinService.getPermissionSchemaSpaceId(headTwin));
        }
    }

    private void compareAndUpdateTwinFields(TwinEntity dbTwinEntity, TwinClassEntity oldTwinClass, TwinClassEntity newTwinClass, TwinChangeClass twinChangeClass, TwinChangesCollector twinChangesCollector) throws ServiceException {
        // Load fields for both classes
        twinClassFieldService.loadTwinClassFields(oldTwinClass);
        twinClassFieldService.loadTwinClassFields(newTwinClass);

        // Get all fields from the old class (including inherited fields)
        Kit<TwinClassFieldEntity, UUID> oldClassFields = oldTwinClass.getTwinClassFieldKit();
        Kit<TwinClassFieldEntity, UUID> newClassFields = newTwinClass.getTwinClassFieldKit();

        // Create sets for store different fields
        Set<UUID> keepFields = new HashSet<>();
        Map<UUID, UUID> replaceFieldClassMap = new HashMap<>();
        Set<UUID> deleteFields = new HashSet<>();

        Map<UUID, TwinClassFieldEntity> newFieldsByUUID = new HashMap<>();
        Map<String, TwinClassFieldEntity> newFieldsByKeyAndTyper = new HashMap<>();
        for (TwinClassFieldEntity newField : newClassFields) {
            //TODO check parameters for field typer list based
            String keyAndTyper = newField.getKey() + "#" + newField.getFieldTyperFeaturerId();
            newFieldsByKeyAndTyper.put(keyAndTyper, newField);
            newFieldsByUUID.put(newField.getId(), newField);
        }

        // Transfer fields by key and fieldTyper
        for (TwinClassFieldEntity oldFieldClass : oldClassFields) {
            //keep fields
            if (newFieldsByUUID.get(oldFieldClass.getId()) != null && oldFieldClass.getId().equals(newFieldsByUUID.get(oldFieldClass.getId()).getId())) {
                keepFields.add(oldFieldClass.getId());
            } else if (newFieldsByKeyAndTyper.get(oldFieldClass.getId()) != null && oldFieldClass.getId().equals(newFieldsByUUID.get(oldFieldClass.getId()).getId())) {
                //TODO need to check ft params
                String keyAndTyperAndParams = oldFieldClass.getKey() + "#" + oldFieldClass.getFieldTyperFeaturerId();
                replaceFieldClassMap.put(oldFieldClass.getId(), newFieldsByKeyAndTyper.get(keyAndTyperAndParams).getId());
            } else if (twinChangeClass.getFieldsReplaceMap() != null && twinChangeClass.getFieldsReplaceMap().containsKey(oldFieldClass.getId())) {
                // todo need to check ft & params
                replaceFieldClassMap.put(oldFieldClass.getId(), twinChangeClass.getFieldsReplaceMap().get(oldFieldClass.getId()));
            } else {
                deleteFields.add(oldFieldClass.getId());
            }
        }

        //TODO process replaceFieldClassMap (dont forget move attachments)

        // Check if we should throw an error for fields that can't be transferred
        if (!deleteFields.isEmpty() && twinChangeClass.getBehavior() != null && twinChangeClass.getBehavior().contains(TwinChangeClassStrategy.throwOnFieldCantBeTransferred))
            throw new ServiceException(ErrorCodeTwins.TWIN_FIELD_VALUE_INCORRECT, "Fields would be lost during class update: " + deleteFields);

        // Remove fields that don't exist in the new class
        if (!deleteFields.isEmpty()) {
            Map<TwinFieldStorage, Set<UUID>> storageToFieldIds = new HashMap<>();
            //group field-classes by storage
            for (UUID fieldId : deleteFields) {
                FieldTyper fieldTyper = featurerService.getFeaturer(oldClassFields.get(fieldId).getFieldTyperFeaturerId(), FieldTyper.class);
                TwinFieldStorage storage = fieldTyper.getStorage(oldClassFields.get(fieldId));
                storageToFieldIds.computeIfAbsent(storage, k -> new HashSet<>()).add(fieldId);
            }
            Map<UUID, Set<UUID>> deleteMap;
            for (Map.Entry<TwinFieldStorage, Set<UUID>> entry : storageToFieldIds.entrySet()) {
                deleteMap = new HashMap<>();
                deleteMap.put(dbTwinEntity.getId(), entry.getValue());
                entry.getKey().deleteTwinFieldsForTwins(deleteMap);
            }
        }
    }

    private void updateStatus(TwinEntity dbTwinEntity, TwinClassEntity newTwinClass, TwinChangeClass twinChangeClass, TwinChangesCollector twinChangesCollector) throws ServiceException {
        // Check if all required fields are filled
        boolean allRequiredFieldsFilled = twinService.isAllRequiredFieldsFilled(dbTwinEntity);
        // Update the status based on required fields
        if (!allRequiredFieldsFilled) {
            if (twinChangeClass.getBehavior() != null && twinChangeClass.getBehavior().contains(TwinChangeClassStrategy.throwOnFieldRequiredNotFilled))
                throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED, "Twin's doesn't has all required fields of the new class.");
            // If not all required fields are filled, set the status to SKETCH
            twinService.setInitSketchStatus(dbTwinEntity);
        } else {
            // Try to keep the current status if it's valid for the new class
            UUID currentStatusId = dbTwinEntity.getTwinStatusId();

            // Load statuses for the new class
            twinStatusService.loadStatusesForTwinClasses(newTwinClass);
            // Check if the current status is valid for the new class
            boolean statusIsValid = false;
            if (newTwinClass.getTwinStatusKit() != null) {
                statusIsValid = newTwinClass.getTwinStatusKit().contains(currentStatusId);
            }

            if (!statusIsValid) {
                // If the current status is not valid, get the default status from the twinflow
                twinflowService.loadTwinflows(newTwinClass);
                if (newTwinClass.getTwinflowKit() != null && !newTwinClass.getTwinflowKit().isEmpty() && !newTwinClass.getTwinflowKit().getList().isEmpty()) {
                    TwinflowEntity twinflow = newTwinClass.getTwinflowKit().getList().get(0);
                    if (twinflow != null && twinflow.getInitialTwinStatusId() != null) {
                        dbTwinEntity.setTwinStatusId(twinflow.getInitialTwinStatusId());
                    } else
                        throw new ServiceException(ErrorCodeTwins.TWIN_STATUS_INCORRECT, "no default status is available for class: " + newTwinClass.getId());

                } else
                    throw new ServiceException(ErrorCodeTwins.TWIN_STATUS_INCORRECT, "no default twinflow is available for class: " + newTwinClass.getId());
            }
        }
    }
}
