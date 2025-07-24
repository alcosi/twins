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

import java.util.Properties;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_3901,
        name = "Class finder by twin class id",
        description = "")
public class ClassFinderGiven extends ClassFinder {
    @FeaturerParam(name = "twin class id", description = "", order = 1)
    public static final FeaturerParamUUID twinClassId = new FeaturerParamUUIDTwinsTwinClassId("twinClassId");
    @FeaturerParam(name = "extendsDepth", description = "extendsDepth", optional = false, order = 2)
    public static final FeaturerParamInt extendsDepth = new FeaturerParamInt("extendsDepth");


    @Override
    protected void createSearch(Properties properties, TwinClassSearch classSearch) throws ServiceException {
        classSearch.addTwinClassId(twinClassId.extract(properties));
        HierarchySearch extendsSearch = new HierarchySearch();
        extendsSearch.setDepth(extendsDepth.extract(properties));
        classSearch.setExtendsHierarchyChildsForTwinClassSearch(extendsSearch);
    }
}
