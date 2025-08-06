package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassId;
import org.twins.core.service.twin.TwinSearchService;
import org.twins.core.service.twin.TwinService;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_2342,
        name = "Field as context field head",
        description = "Get head for twin from src field(link). Set this head to dst field(link)")
@Slf4j
public class FillerFieldAsFoundTwinOfClass extends Filler {

    @FeaturerParam(name = "Twin class field id", description = "", order = 1)
    public static final FeaturerParamUUID twinClassFieldLinkId = new FeaturerParamUUIDTwinsTwinClassFieldId("twinClassFieldLinkId");

    @FeaturerParam(name = "Twin class id", description = "", order = 2)
    public static final FeaturerParamUUID twinClassId = new FeaturerParamUUIDTwinsTwinClassId("twinClassId");

    @Lazy
    @Autowired
    TwinSearchService twinSearchService;

    @Lazy
    @Autowired
    TwinService twinService;

    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        BasicSearch search = new BasicSearch();

        search
                .addTwinClassId(twinClassId.extract(properties), false);

        TwinEntity twinEntity = twinSearchService.findTwins(search).getFirst();

        FieldValue fieldValue = twinService.createFieldValue(twinClassFieldLinkId.extract(properties), twinEntity.getId().toString());

        factoryItem.getOutput().addField(fieldValue);
    }
}
