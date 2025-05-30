package org.twins.core.featurer.factory.filler;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_2320,
        name = "Attachment CUD from context",
        description = "")
public class FillerAttachmentCUDFromContext extends Filler {
    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        if (factoryItem.getFactoryContext().getAttachmentCUD() == null)
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "Not attachments in context");
        if (factoryItem.getOutput() instanceof TwinUpdate twinUpdate)
            twinUpdate.setAttachmentCUD(factoryItem.getFactoryContext().getAttachmentCUD().clone());
        else if (factoryItem.getOutput() instanceof TwinCreate twinCreate) {
            twinCreate.setAttachmentEntityList(factoryItem.getFactoryContext().getAttachmentCUD().clone().getCreateList());
        }
    }
}
