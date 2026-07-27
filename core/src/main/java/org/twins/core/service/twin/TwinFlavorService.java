package org.twins.core.service.twin;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.StringUtils;
import org.cambium.common.util.UuidUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.EntityRelinkOperation;
import org.twins.core.enums.EntityRelinkOperationStrategy;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.datalist.DataListService;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Lazy
@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinFlavorService {
    final TwinRepository twinRepository;
    final DataListService dataListService;

    @Transactional(rollbackFor = Throwable.class)
    public void replaceFlavorForTwinsOfClass(TwinClassEntity twinClassEntity, EntityRelinkOperation entityRelinkOperation) throws ServiceException {
        if (UuidUtils.isNullifyMarker(entityRelinkOperation.getNewId())) {
            //we have to clear flavor on all twins of given class
            twinRepository.clearFlavorForTwinsOfClass(twinClassEntity.getId());
            twinClassEntity
                    .setFlavorDataListId(null)
                    .setFlavorDataList(null);
            return;
        }
        DataListEntity newFlavorDataList = dataListService.findEntitySafe(entityRelinkOperation.getNewId());
        //we will try to replace flavor on twins with new provided values
        Set<UUID> existedTwinFlavorIds = twinRepository.findDistinctFlavorDataListOptionIdByTwinClassId(twinClassEntity.getId());
        if (CollectionUtils.isEmpty(existedTwinFlavorIds)) {
            twinClassEntity
                    .setFlavorDataList(newFlavorDataList)
                    .setFlavorDataListId(newFlavorDataList.getId());
            return; // nice :) we have nothing to do
        }

        if (entityRelinkOperation.getStrategy() == EntityRelinkOperationStrategy.restrict
                && MapUtils.isEmpty(entityRelinkOperation.getReplaceMap()))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_UPDATE_RESTRICTED, "please provide flavorReplaceMap for flavors: " + StringUtils.join(existedTwinFlavorIds));

        dataListService.loadDataListOptions(newFlavorDataList);
        Set<UUID> flavorsForClearing = new HashSet<>();
        for (UUID flavorForReplace : existedTwinFlavorIds) {
            if (newFlavorDataList.getOptions().get(flavorForReplace) != null) //be smart if somehow already existed flavor belongs to new list
                continue;
            UUID replacement = entityRelinkOperation.getReplaceMap().get(flavorForReplace);
            if (replacement == null) {
                if (entityRelinkOperation.getStrategy() == EntityRelinkOperationStrategy.restrict)
                    throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_UPDATE_RESTRICTED, "please provide flavorReplaceMap value for flavor: " + flavorForReplace);
                else
                    replacement = UuidUtils.NULLIFY_MARKER;
            }
            if (UuidUtils.isNullifyMarker(replacement)) {
                flavorsForClearing.add(flavorForReplace);
                continue;
            }
            if (newFlavorDataList.getOptions().get(replacement) == null)
                throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_UPDATE_RESTRICTED, "please provide correct flavorReplaceMap value for flavor: " + flavorForReplace);
            twinRepository.replaceFlavorForTwinsOfClass(twinClassEntity.getId(), flavorForReplace, replacement);
        }
        if (CollectionUtils.isNotEmpty(flavorsForClearing)) {
            for (UUID flavorForClearing : flavorsForClearing)
                twinRepository.replaceFlavorForTwinsOfClass(twinClassEntity.getId(), flavorForClearing, null);
        }
        twinClassEntity
                .setFlavorDataList(newFlavorDataList)
                .setFlavorDataListId(newFlavorDataList.getId());
    }
}
