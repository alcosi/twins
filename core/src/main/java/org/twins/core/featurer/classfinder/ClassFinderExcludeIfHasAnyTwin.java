package org.twins.core.featurer.classfinder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.domain.search.HierarchySearch;
import org.twins.core.domain.search.TwinClassSearch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsClassId;
import org.twins.core.service.twin.TwinSearchService;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_3904,
        name = "Exclude twin class if has any twin",
        description = "")
@RequiredArgsConstructor
public class ClassFinderExcludeIfHasAnyTwin extends ClassFinder {
    @FeaturerParam(name = "twin class id set", description = "", order = 1, optional = false)
    public static final FeaturerParamUUIDSet twinClassIds = new FeaturerParamUUIDSetTwinsClassId("twinClassIdSet");

    private final TwinSearchService twinSearchService;

    @Override
    protected void concatSearch(Properties properties, TwinClassSearch classSearch) throws ServiceException {
        Set<UUID> twinClassIdSet = twinClassIds.extract(properties);
        if (twinClassIdSet.isEmpty()) {
            return;
        }
        BasicSearch twinSearch = new BasicSearch();
        twinSearch.setTwinClassExtendsHierarchyContainsIdList(twinClassIdSet);
        var countGrouped = twinSearchService.countGroupBy(twinSearch, TwinEntity.Fields.twinClass);
        Set<UUID> excludeTwinClasses = new HashSet<>();
        for (var entry : countGrouped.entrySet()) {
            if (entry.getValue() == 0)
                continue;
            for (var twinClassId : twinClassIdSet) {
                if (((TwinClassEntity)entry.getKey()).getExtendedClassIdSet().contains(twinClassId))
                    excludeTwinClasses.add(twinClassId);
            }
        }
        if (!excludeTwinClasses.isEmpty()) {
            if (classSearch.getExtendsHierarchyChildsForTwinClassSearch() != null) {
                throw new ServiceException(ErrorCodeTwins.CONFIGURATION_IS_INVALID, "class extends hierarchy is already filled");
            }
            HierarchySearch hierarchySearch = new HierarchySearch()
                    .setIdExcludeList(excludeTwinClasses)
                    .setDepth(0);
            classSearch
                    .addTwinClassId(excludeTwinClasses, true)
                    .setExtendsHierarchyChildsForTwinClassSearch(hierarchySearch);
        }
    }
}
