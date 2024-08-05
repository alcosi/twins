package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.TwinBasicFields;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Properties;

import static org.cambium.common.util.UuidUtils.NULLIFY_MARKER;

@Component
@Featurer(id = FeaturerTwins.ID_2327,
        name = "FillerTwinBasicFieldsFromContextBasics",
        description = "")
@Slf4j
public class FillerTwinBasicFieldsFromContextBasics extends Filler {

    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        TwinEntity outputTwinEntity = factoryItem.getOutput().getTwinEntity();
        TwinBasicFields basics = factoryItem.getFactoryContext().getBasics();
        if(null != basics) {
            if(null != basics.getCreatedByUserId()) {
                if(basics.getCreatedByUserId().equals(NULLIFY_MARKER))
                    outputTwinEntity.setCreatedByUserId(null);
                else
                    outputTwinEntity.setCreatedByUserId(basics.getCreatedByUserId());
            }
            if(null != basics.getAssignerUserId()) {
                if(basics.getAssignerUserId().equals(NULLIFY_MARKER))
                    outputTwinEntity.setAssignerUser(null);
                else
                    outputTwinEntity.setAssignerUserId(basics.getAssignerUserId());
            }
            if(null != basics.getName()) outputTwinEntity.setName(basics.getName());
            if(null != basics.getDescription()) outputTwinEntity.setDescription(basics.getDescription());
        }
    }
}
