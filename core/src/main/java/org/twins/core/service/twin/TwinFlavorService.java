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
import org.twins.core.dao.specifications.twinclass.TwinClassSpecification;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassRepository;
import org.twins.core.domain.EntityRelinkOperation;
import org.twins.core.enums.EntityRelinkOperationStrategy;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.datalist.DataListService;

import java.util.*;

@Lazy
@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinFlavorService {
    final TwinRepository twinRepository;
    final TwinClassRepository twinClassRepository;
    final DataListService dataListService;

    @Transactional(rollbackFor = Throwable.class)
    public void replaceFlavorForTwinsOfClass(TwinClassEntity twinClassEntity, EntityRelinkOperation entityRelinkOperation) throws ServiceException {
        UUID oldFlavorDataListId = twinClassEntity.getFlavorDataListId();
        boolean disabling = UuidUtils.isNullifyMarker(entityRelinkOperation.getNewId());
        DataListEntity newFlavorDataList = disabling ? null : dataListService.findEntitySafe(entityRelinkOperation.getNewId());
        if (!disabling)
            dataListService.loadDataListOptions(newFlavorDataList);
        boolean restrict = entityRelinkOperation.getStrategy() == EntityRelinkOperationStrategy.restrict;

        // Affected classes = the updated class itself + the descendants that inherit their flavor from
        // it. Collected up front so the twin migration below runs as a single batched pass over the
        // whole set (one query per step, regardless of how many descendants inherit flavor) instead of
        // once per class. The trigger only updates descendants' inherited_* columns — their twins still
        // carry the old flavorDataListOptionId and would fail checkFlavorDataListOption otherwise.
        // A descendant is affected only if it has no flavor list of its own (otherwise it overrides
        // inheritance) and its flavor source is exactly this class.
        Set<UUID> affectedClassIds = new LinkedHashSet<>();
        affectedClassIds.add(twinClassEntity.getId());
        boolean parentHadFlavor = oldFlavorDataListId != null;
        for (TwinClassEntity descendant : twinClassRepository.findAll(TwinClassSpecification.checkExtendsTwinClassChildren(
                Collections.singleton(twinClassEntity.getId()), false, null))) {
            if (descendant.getId().equals(twinClassEntity.getId()))
                continue;
            if (descendant.getFlavorDataListId() != null)
                continue; // the descendant overrides inheritance with its own flavor list -> not affected
            boolean inheritsFromThisClass = parentHadFlavor
                    ? twinClassEntity.getId().equals(descendant.getInheritedFlavorDataListTwinClassId())
                    : descendant.getInheritedFlavorDataListTwinClassId() == null;
            if (inheritsFromThisClass)
                affectedClassIds.add(descendant.getId());
        }

        migrateTwinsFlavor(affectedClassIds, newFlavorDataList, entityRelinkOperation, restrict);

        // set the updated class's OWN flavor list; descendants get their inherited_* via the DB trigger
        if (disabling)
            twinClassEntity.setFlavorDataListId(null).setFlavorDataList(null);
        else
            twinClassEntity.setFlavorDataList(newFlavorDataList).setFlavorDataListId(newFlavorDataList.getId());
    }

    /**
     * Migrate (or clear) the flavor of every twin of the given classes in a single batched pass,
     * honoring the onUnreplacedStrategy contract like marker/tag. Flavor is a mandatory scalar on the
     * twin, so an obsolete unmapped flavor cannot be cleared — the twins holding it are deleted. Does
     * NOT touch any class's own flavorDataListId (that is set by the caller / the DB trigger).
     */
    private void migrateTwinsFlavor(Set<UUID> twinClassIds, DataListEntity newFlavorDataList,
                                    EntityRelinkOperation entityRelinkOperation, boolean restrict) throws ServiceException {
        if (newFlavorDataList == null) {
            // flavor is leaving the inheritance chain -> clear it on the affected classes' twins
            twinRepository.clearFlavorForTwinsOfClassIn(twinClassIds);
            return;
        }
        Set<UUID> existedTwinFlavorIds = twinRepository.findDistinctFlavorDataListOptionIdByTwinClassIdIn(twinClassIds);
        Set<UUID> obsoleteFlavorsForTwinDeletion = new HashSet<>();
        for (UUID oldFlavor : existedTwinFlavorIds) {
            if (newFlavorDataList.getOptions().get(oldFlavor) != null)
                continue; // already a valid option of the new list, nothing to migrate
            UUID replacement = entityRelinkOperation.getReplaceMap().get(oldFlavor);
            if (replacement != null && UuidUtils.isNullifyMarker(replacement)) {
                obsoleteFlavorsForTwinDeletion.add(oldFlavor); // explicitly mapped to NULLIFY_MARKER -> drop the holding twins
                continue;
            }
            if (replacement == null) {
                if (restrict)
                    throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_UPDATE_RESTRICTED,
                            "onUnreplacedStrategy=restrict: please provide a flavorReplaceMap entry for flavor ["
                                    + oldFlavor + "] pointing at an option of the new flavor list");
                obsoleteFlavorsForTwinDeletion.add(oldFlavor); // delete strategy -> drop the holding twins
                continue;
            }
            if (newFlavorDataList.getOptions().get(replacement) == null)
                throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_UPDATE_RESTRICTED,
                        "flavorReplaceMap points flavor [" + oldFlavor + "] to [" + replacement
                                + "] which is not an option of the new flavor list");
            twinRepository.replaceFlavorForTwinsOfClassIn(twinClassIds, oldFlavor, replacement);
        }
        if (!obsoleteFlavorsForTwinDeletion.isEmpty()) {
            int deletedTwins = twinRepository.deleteTwinsByTwinClassIdInAndFlavorDataListOptionIdIn(twinClassIds, obsoleteFlavorsForTwinDeletion);
            log.warn("Deleted {} twin(s) of classes {} holding obsolete unmapped flavors {}",
                    deletedTwins, twinClassIds, obsoleteFlavorsForTwinDeletion);
        }
        // Twins with no flavor never held an obsolete value, so under either strategy they are
        // back-filled with the NULLIFY_MARKER default rather than deleted. Flavor is mandatory and
        // cannot be left NULL.
        long twinsWithoutFlavor = twinRepository.countByTwinClassIdInAndFlavorDataListOptionIdIsNull(twinClassIds);
        if (twinsWithoutFlavor > 0) {
            UUID defaultFlavor = entityRelinkOperation.getReplaceMap().get(UuidUtils.NULLIFY_MARKER);
            if (defaultFlavor == null
                    || UuidUtils.isNullifyMarker(defaultFlavor)
                    || newFlavorDataList.getOptions().get(defaultFlavor) == null)
                throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_UPDATE_RESTRICTED,
                        "flavor is mandatory for twins: " + twinsWithoutFlavor
                                + " twin(s) have no flavorDataListOptionId. "
                                + "Provide a flavorReplaceMap entry [" + UuidUtils.NULLIFY_MARKER
                                + " -> option of the new flavor list] to assign them a flavor.");
            twinRepository.setFlavorForTwinsWithoutFlavorIn(twinClassIds, defaultFlavor);
        }
    }
}
