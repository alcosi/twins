package org.twins.core.featurer.pointer;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassId;
import org.twins.core.service.twin.TwinSearchService;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_3105,
        name = "Direct single grandchild",
        description = "")
@RequiredArgsConstructor
public class PointerOnSingleGrandChild extends Pointer {
    @FeaturerParam(name = "Grandchild twin class", description = "", order = 1)
    public static final FeaturerParamUUID twinClassId = new FeaturerParamUUIDTwinsTwinClassId("grandChildTwinClassId");

    @Lazy
    private final TwinSearchService twinSearchService;

    @Override
    protected TwinEntity point(Properties properties, TwinEntity srcTwinEntity) throws ServiceException {
        BasicSearch childSearch = new BasicSearch();
        childSearch.addHeadTwinId(srcTwinEntity.getId());

        List<TwinEntity> children = twinSearchService.findTwins(childSearch);

        if (CollectionUtils.isEmpty(children)) {
            return null;
        }

        List<TwinEntity> grandchildren = new ArrayList<>();

        for (TwinEntity child : children) {
            BasicSearch grandchildSearch = new BasicSearch();
            grandchildSearch
                    .addHeadTwinId(child.getId())
                    .addTwinClassId(twinClassId.extract(properties), false);

            List<TwinEntity> found = twinSearchService.findTwins(grandchildSearch);
            if (!CollectionUtils.isEmpty(found)) {
                grandchildren.addAll(found);
            }
        }

        if (CollectionUtils.isEmpty(grandchildren)) {
            return null;
        } else if (grandchildren.size() > 1) {
            throw new ServiceException(ErrorCodeTwins.POINTER_NON_SINGLE, srcTwinEntity.logShort() + " has " + grandchildren.size() + " grandchild twins of class[" + twinClassId.extract(properties) + "]");
        } else {
            return grandchildren.getFirst();
        }
    }
}
