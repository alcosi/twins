package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsLinkId;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;

import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_2343,
        name = "Basics assignee from output twin field link",
        description = "")
@Slf4j
public class FillerBasicsAssigneeFromOutputTwinFieldLink extends Filler {

    @FeaturerParam(name = "twin class field id", description = "", order = 1)
    public static final FeaturerParamUUID twinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("twinClassFieldId");

    @FeaturerParam(name = "link id", description = "", order = 2)
    public static final FeaturerParamUUID linkId = new FeaturerParamUUIDTwinsLinkId("linkId");

    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        TwinEntity outputTwinEntity = factoryItem.getOutput().getTwinEntity();

        FieldValue field = factoryItem.getOutput().getField(twinClassFieldId.extract(properties));
        if (field == null) {
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "Field is not of type link");
        }
        if (field instanceof FieldValueLink itemOutputFieldLink) {
            if (itemOutputFieldLink.isEmpty()) {
                throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "Twin does not contain a links");
            }
            boolean isLink = false;
            UUID extractedLinkId = linkId.extract(properties);
            for (TwinLinkEntity twinLink : itemOutputFieldLink.getItems()) {
                if (twinLink.getLinkId().equals(extractedLinkId)) {
                    TwinEntity dstTwin = twinLink.getDstTwin();
                    outputTwinEntity
                            .setAssignerUser(dstTwin.getAssignerUser())
                            .setAssignerUserId(dstTwin.getAssignerUserId());
                    isLink = true;
                }
            }
            if (!isLink) {
                throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "In output twin is missing a link[" + extractedLinkId + "]");
            }
        }
    }
}
