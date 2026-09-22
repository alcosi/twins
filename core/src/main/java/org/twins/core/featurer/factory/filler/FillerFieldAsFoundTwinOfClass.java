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
import org.twins.core.domain.factory.FactoryItemsBatch;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassId;
import org.twins.core.service.twin.TwinSearchService;
import org.twins.core.service.twin.TwinService;

import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_2342,
        name = "Field as found twin of class",
        description = "Fill link field by found twin of class")
@Slf4j
public class FillerFieldAsFoundTwinOfClass extends Filler {

    @FeaturerParam(name = "Twin class field id", description = "Link field witch need to fill", order = 1)
    public static final FeaturerParamUUID twinClassFieldLinkId = new FeaturerParamUUIDTwinsTwinClassFieldId("twinClassFieldLinkId");

    @FeaturerParam(name = "Twin class id", description = "Class of twin that will fill the field", order = 2)
    public static final FeaturerParamUUID twinClassId = new FeaturerParamUUIDTwinsTwinClassId("twinClassId");

    @Lazy
    @Autowired
    TwinSearchService twinSearchService;

    @Lazy
    @Autowired
    TwinService twinService;

    /**
     * Direct batch override (not a {@code FillerAtomic} subclass): the search is keyed only by params,
     * so one query answers every item of the batch — see featurer_design_pattern.md.
     */
    @Override
    public void fill(Properties properties, FactoryItemsBatch batch, TwinEntity templateTwin, boolean optionalStep) throws ServiceException {
        if (batch == null || batch.isEmpty())
            return;
        UUID extractedTwinClassId = twinClassId.extract(properties);
        BasicSearch search = new BasicSearch();
        search
                .addTwinClassId(extractedTwinClassId, false);
        var entityList = twinSearchService.findTwins(search); // ONE query for the whole batch
        if (entityList.isEmpty()) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "there are no twins of class[" + extractedTwinClassId + "] found.");
        }
        if (entityList.size() > 1) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "there are more than one twin of class[" + extractedTwinClassId + "] found.");
        }
        UUID fieldId = twinClassFieldLinkId.extract(properties);
        for (FactoryItem factoryItem : batch.getFactoryItems()) {
            try {
                fillWithFoundTwin(factoryItem, fieldId, entityList.getFirst());
            } catch (Exception ex) {
                if (optionalStep) {
                    log.warn("Step is optional and unsuccessful for {}: {}. Pipeline will not be aborted",
                            factoryItem.logShort(),
                            ex instanceof ServiceException serviceException ? serviceException.getErrorLocation() : ex.getMessage());
                } else {
                    throw ex;
                }
            }
        }
    }

    protected void fillWithFoundTwin(FactoryItem factoryItem, UUID fieldId, TwinEntity foundTwin) throws ServiceException {
        FieldValue fieldValue = twinService.createFieldValue(fieldId, foundTwin);
        factoryItem.getOutput().addField(fieldValue);
    }
}
