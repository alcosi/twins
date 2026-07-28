package org.twins.core.service.twin;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.UuidUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.EntityRelinkOperation;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.datalist.DataListService;

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
            // flavor is being disabled on the class -> clear it on every twin.
            // Once the class has no flavor list, twins without a flavor become valid again.
            twinRepository.clearFlavorForTwinsOfClass(twinClassEntity.getId());
            twinClassEntity
                    .setFlavorDataListId(null)
                    .setFlavorDataList(null);
            return;
        }
        DataListEntity newFlavorDataList = dataListService.findEntitySafe(entityRelinkOperation.getNewId());
        dataListService.loadDataListOptions(newFlavorDataList);

        // Flavor is MANDATORY on a twin once its class has a flavor list, so unlike marker/tag
        // (optional) we may never clear/nullify a twin's flavor — that would turn it invalid.
        // Every existing flavor that is not kept by the new list must therefore be mapped to a
        // valid option of the new list; otherwise the whole class update is rejected.
        Set<UUID> existedTwinFlavorIds = twinRepository.findDistinctFlavorDataListOptionIdByTwinClassId(twinClassEntity.getId());
        for (UUID oldFlavor : existedTwinFlavorIds) {
            if (newFlavorDataList.getOptions().get(oldFlavor) != null)
                continue; // already a valid option of the new list, nothing to migrate
            UUID replacement = entityRelinkOperation.getReplaceMap().get(oldFlavor);
            if (replacement == null || UuidUtils.isNullifyMarker(replacement) || newFlavorDataList.getOptions().get(replacement) == null)
                throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_UPDATE_RESTRICTED,
                        "flavor is mandatory for twins; please provide a valid flavorReplaceMap entry for flavor ["
                                + oldFlavor + "] pointing at an option of the new flavor list (a flavor cannot be cleared)");
            twinRepository.replaceFlavorForTwinsOfClass(twinClassEntity.getId(), oldFlavor, replacement);
        }

        // Twins that currently have no flavor would become invalid as soon as the class requires one.
        // They can be back-filled in place through the replaceMap: an entry with the NULLIFY_MARKER
        // key acts as the default flavor assigned to every flavor-less twin (NULLIFY_MARKER -> new
        // option). Without such an entry the update is rejected, since a mandatory flavor cannot be
        // left NULL.
        long twinsWithoutFlavor = twinRepository.countByTwinClassIdAndFlavorDataListOptionIdIsNull(twinClassEntity.getId());
        if (twinsWithoutFlavor > 0) {
            UUID defaultForNullFlavor = entityRelinkOperation.getReplaceMap().get(UuidUtils.NULLIFY_MARKER);
            if (defaultForNullFlavor == null
                    || UuidUtils.isNullifyMarker(defaultForNullFlavor)
                    || newFlavorDataList.getOptions().get(defaultForNullFlavor) == null)
                throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_UPDATE_RESTRICTED,
                        "flavor is mandatory for twins: " + twinsWithoutFlavor
                                + " twin(s) of the class have no flavorDataListOptionId. "
                                + "Provide a flavorReplaceMap entry [" + UuidUtils.NULLIFY_MARKER
                                + " -> option of the new flavor list] to assign them a flavor.");
            twinRepository.setFlavorForTwinsWithoutFlavor(twinClassEntity.getId(), defaultForNullFlavor);
        }

        twinClassEntity
                .setFlavorDataList(newFlavorDataList)
                .setFlavorDataListId(newFlavorDataList.getId());
    }
}
