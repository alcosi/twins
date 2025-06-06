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

import java.util.List;
import java.util.Properties;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_3104,
        name = "Direct single child",
        description = "")
@RequiredArgsConstructor
public class PointerOnSingleChild extends Pointer {
    @FeaturerParam(name = "Child twin class", description = "", order = 1)
    public static final FeaturerParamUUID twinClassId = new FeaturerParamUUIDTwinsTwinClassId("childTwinClassId");

    @Lazy
    private final TwinSearchService twinSearchService;

    @Override
    protected TwinEntity point(Properties properties, TwinEntity srcTwinEntity) throws ServiceException {
        BasicSearch basicSearch = new BasicSearch();
        basicSearch
                .addHeadTwinId(srcTwinEntity.getId())
                .addTwinClassExtendsHierarchyContainsId(twinClassId.extract(properties));
        List<TwinEntity> childTwins = twinSearchService.findTwins(basicSearch);
        if (CollectionUtils.isEmpty(childTwins))
            return null;
        else if (childTwins.size() > 1)
            throw new ServiceException(ErrorCodeTwins.POINTER_NON_SINGLE, srcTwinEntity.logShort() + " has " + childTwins.size() + " child twins of class[" + twinClassId.extract(properties) + "]");
        else
            return childTwins.getFirst();
    }
}
