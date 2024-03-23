package org.twins.core.featurer.fieldtyper;

import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.util.ObjectUtils;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldEntity;
import org.twins.core.dao.twin.TwinFieldRepository;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.Properties;

public interface FieldTyperCountChildrenTwins {

    @FeaturerParam(name = "childrenTwinStatusIdList", description = "Twin.Status.IDs of child twin")
    FeaturerParamUUIDSet childrenTwinStatusIdList = new FeaturerParamUUIDSet("childrenTwinStatusIdList");

    @FeaturerParam(name = "exclude", description = "Exclude(true)/Include(false) child-field's Twin.Status.IDs from query result")
    FeaturerParamBoolean exclude = new FeaturerParamBoolean("exclude");

    default Long parseTwinFieldValue(TwinFieldEntity twinFieldEntity) throws ServiceException {
        long result = 0;
        if(null != twinFieldEntity.getValue()){
            try {
                if(!ObjectUtils.isEmpty(twinFieldEntity.getValue())) result = Long.parseLong(twinFieldEntity.getValue());
            } catch (NumberFormatException e) {
                throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT, twinFieldEntity.easyLog(EasyLoggable.Level.NORMAL) + " value[" + twinFieldEntity.getValue() + "] cant be parsed to Long");
            }
        }
        return result;
    }

    default Long getCountResult(Properties properties, TwinEntity twinEntity, TwinFieldRepository twinFieldRepository) {
        return exclude.extract(properties) ?
                twinFieldRepository.countChildrenTwinsWithStatusNotIn(twinEntity.getId(), childrenTwinStatusIdList.extract(properties)) :
                twinFieldRepository.countChildrenTwinsWithStatusIn(twinEntity.getId(), childrenTwinStatusIdList.extract(properties));
    }


}
