package org.twins.core.featurer.fieldtyper;

import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.util.ObjectUtils;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldSimpleEntity;
import org.twins.core.dao.twin.TwinFieldSimpleRepository;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsStatusId;

import java.util.Properties;

public interface FieldTyperCountChildrenByStatus {

    @FeaturerParam(name = "childrenTwinStatusIdList", description = "Twin.Status.IDs of child twin")
    FeaturerParamUUIDSet childrenTwinStatusIdList = new FeaturerParamUUIDSetTwinsStatusId("childrenTwinStatusIdList");

    @FeaturerParam(name = "exclude", description = "Exclude(true)/Include(false) child-field's Twin.Status.IDs from query result")
    FeaturerParamBoolean exclude = new FeaturerParamBoolean("exclude");

    default Long parseTwinFieldValue(TwinFieldSimpleEntity twinFieldEntity) throws ServiceException {
        long result = 0;
        if(twinFieldEntity != null && null != twinFieldEntity.getValue()){
            try {
                if(!ObjectUtils.isEmpty(twinFieldEntity.getValue())) result = Long.parseLong(twinFieldEntity.getValue());
            } catch (NumberFormatException e) {
                throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT, twinFieldEntity.easyLog(EasyLoggable.Level.NORMAL) + " value[" + twinFieldEntity.getValue() + "] cant be parsed to Long");
            }
        }
        return result;
    }

    default Long getCountResult(Properties properties, TwinEntity twinEntity, TwinFieldSimpleRepository twinFieldSimpleRepository) {
        return exclude.extract(properties) ?
                twinFieldSimpleRepository.countChildrenTwinsWithStatusNotIn(twinEntity.getId(), childrenTwinStatusIdList.extract(properties)) :
                twinFieldSimpleRepository.countChildrenTwinsWithStatusIn(twinEntity.getId(), childrenTwinStatusIdList.extract(properties));
    }


}
