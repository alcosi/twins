package org.twins.core.featurer.classfinder;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.HierarchySearch;
import org.twins.core.domain.search.TwinClassSearch;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassId;

import java.util.HashSet;
import java.util.Properties;
import java.util.UUID;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_3901,
        name = "Class finder by twin class id",
        description = "")
public class ClassFinderExtendsHierarchyChildrenOf extends ClassFinder {
    @FeaturerParam(name = "twin class id", description = "", order = 1)
    public static final FeaturerParamUUID twinClassId = new FeaturerParamUUIDTwinsTwinClassId("twinClassId");
    @FeaturerParam(name = "extendsDepth", description = "extendsDepth", optional = true, defaultValue = "0", order = 2)
    public static final FeaturerParamInt extendsDepth = new FeaturerParamInt("extendsDepth");


    @Override
    protected void concatSearch(Properties properties, TwinClassSearch classSearch) throws ServiceException {
        HashSet<UUID> twinClassIdSet = new HashSet<>();
        twinClassIdSet.add(twinClassId.extract(properties));
        HierarchySearch extendsSearch = new HierarchySearch()
                .setIdList(twinClassIdSet)
                .setDepth(extendsDepth.extract(properties));
        classSearch.setExtendsHierarchyChildsForTwinClassSearch(extendsSearch);
    }
}
