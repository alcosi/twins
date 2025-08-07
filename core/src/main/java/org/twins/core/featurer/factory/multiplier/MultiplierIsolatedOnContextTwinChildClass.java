package org.twins.core.featurer.factory.multiplier;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.search.HierarchySearch;
import org.twins.core.domain.search.TwinClassSearch;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.twinclass.TwinClassSearchService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

@Component
@Featurer(id = FeaturerTwins.ID_2210,
        name = "Isolated on context head twin",
        description = "New output twin for each input. Output class is child of context twin's class")
public class MultiplierIsolatedOnContextTwinChildClass extends Multiplier {

    @FeaturerParam(name = "Create twin sketch", description = "If true, create twin sketch instead of twin", order = 1, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean sketchMode = new FeaturerParamBoolean("sketchMode");

    @Lazy
    @Autowired
    TwinClassSearchService twinClassSearchService;

    public List<FactoryItem> multiply(Properties properties, List<FactoryItem> inputFactoryItemList, FactoryContext factoryContext) throws ServiceException {
        List<FactoryItem> ret = new ArrayList<>();

        for (FactoryItem inputFactoryItem : inputFactoryItemList) {
            UUID extractedTwinClassId = inputFactoryItem.getTwin().getTwinClassId();

            HierarchySearch hierarchySearch = new HierarchySearch();
            hierarchySearch
                    .setIdList(Set.of(extractedTwinClassId))
                    .setDepth(1);

            TwinClassSearch twinClassSearch = new TwinClassSearch();
            twinClassSearch
                    .setHeadHierarchyChildsForTwinClassSearch(hierarchySearch);

            List<TwinClassEntity> entityList = twinClassSearchService.searchTwinClasses(twinClassSearch);

            if (entityList.isEmpty()) {
                throw new ServiceException(ErrorCodeTwins.FACTORY_MULTIPLIER_ERROR, "there are no child twin classes of class[" + extractedTwinClassId + "] found.");
            }
            if (entityList.size() > 1) {
                throw new ServiceException(ErrorCodeTwins.FACTORY_MULTIPLIER_ERROR, "there are more than one child twin classes of class[" + extractedTwinClassId + "] found.");
            }

            TwinEntity twinEntity = new TwinEntity()
                    .setName("")
                    .setTwinClass(entityList.getFirst())
                    .setTwinClassId(entityList.getFirst().getId())
                    .setHeadTwinId(inputFactoryItem.getTwin().getId())
                    .setHeadTwin(inputFactoryItem.getTwin())
                    .setCreatedAt(Timestamp.from(Instant.now()));

            TwinCreate twinCreate = new TwinCreate();
            twinCreate
                    .setSketchMode(sketchMode.extract(properties))
                    .setTwinEntity(twinEntity);

            ret.add(new FactoryItem().setOutput(twinCreate));
        }

        return ret;
    }
}
