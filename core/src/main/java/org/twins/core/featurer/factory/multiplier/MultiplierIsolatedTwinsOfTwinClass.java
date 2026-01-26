package org.twins.core.featurer.factory.multiplier;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassId;
import org.twins.core.service.twin.TwinSearchService;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Featurer(id = FeaturerTwins.ID_2211,
        name = "Isolated twins of twin class",
        description = "Output list of twins for each input. Twins will be loaded by twin class from params")
public class MultiplierIsolatedTwinsOfTwinClass extends Multiplier {
    @FeaturerParam(name = "Twin class id", description = "", order = 1)
    public static final FeaturerParamUUID twinClassId = new FeaturerParamUUIDTwinsTwinClassId("twinClassId");

    private final TwinSearchService twinSearchService;

    @Override
    public List<FactoryItem> multiply(Properties properties, List<FactoryItem> inputFactoryItemList, FactoryContext factoryContext) throws ServiceException {
        List<FactoryItem> ret = new ArrayList<>();

        BasicSearch search = new BasicSearch();
        search
                .addOwnerBusinessAccountId(factoryContext.getRunLimitedByOwnerBusinessAccount())
                .setTwinClassExtendsHierarchyContainsIdList(Set.of(twinClassId.extract(properties)));

        List<TwinEntity> twins = twinSearchService.findTwins(search);

        for (TwinEntity twinEntity : twins) {
            TwinUpdate twinUpdate = new TwinUpdate();
            twinUpdate
                    .setDbTwinEntity(twinEntity) // original twin
                    .setTwinEntity(twinEntity.clone()); // collecting updated in new twin
            ret.add(new FactoryItem().setOutput(twinUpdate).setContextFactoryItemList(inputFactoryItemList));
        }

        return ret;
    }
}
