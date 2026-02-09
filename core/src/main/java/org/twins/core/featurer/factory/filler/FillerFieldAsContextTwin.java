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
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;
import org.twins.core.service.twin.TwinService;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_2345,
        name = "Field as single context twin",
        description = "Fill link field as single context twin")
@Slf4j
public class FillerFieldAsContextTwin extends Filler {
    @FeaturerParam(name = "Twin class field id", description = "Link field witch need to fill", order = 1)
    public static final FeaturerParamUUID twinClassFieldLinkId = new FeaturerParamUUIDTwinsTwinClassFieldId("twinClassFieldLinkId");

    @Lazy
    @Autowired
    TwinService twinService;

    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        var twin = factoryItem.checkSingleContextTwin();
        FieldValue fieldValue = twinService.createFieldValue(twinClassFieldLinkId.extract(properties), twin.getId().toString());
        factoryItem.getOutput().addField(fieldValue);
    }
}
