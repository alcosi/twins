package org.twins.core.featurer.fieldtyper;

import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.util.ObjectUtils;
import org.twins.core.dao.twin.TwinFieldEntity;
import org.twins.core.dao.twin.TwinFieldRepository;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.Properties;

public interface FieldTyperCalcChildrenField {
    @FeaturerParam(name = "childrenTwinClassFieldId", description = "Twin.Class.Field Id of child twin fields")
    FeaturerParamUUID childrenTwinClassFieldId = new FeaturerParamUUID("childrenTwinClassFieldId");

    @FeaturerParam(name = "childrenTwinStatusIdList", description = "Twin.Status.IDs of child twin")
    FeaturerParamUUIDSet childrenTwinStatusIdList = new FeaturerParamUUIDSet("childrenTwinStatusIdList");

    @FeaturerParam(name = "exclude", description = "Exclude(true)/Include(false) child-field's Twin.Status.IDs from query result")
    FeaturerParamBoolean exclude = new FeaturerParamBoolean("exclude");

    default Double parseTwinFieldValue(TwinFieldEntity twinFieldEntity) throws ServiceException {
        double result = 0d;
        if(null != twinFieldEntity.getValue()){
            try {
                if(!ObjectUtils.isEmpty(twinFieldEntity.getValue())) result = Double.parseDouble(twinFieldEntity.getValue());
            } catch (NumberFormatException e) {
                throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT, twinFieldEntity.easyLog(EasyLoggable.Level.NORMAL) + " value[" + twinFieldEntity.getValue() + "] cant be parsed to Double");
            }
        }
        return result;
    }

    default Double getSumResult(Properties properties, TwinFieldEntity twinFieldEntity, TwinFieldRepository twinFieldRepository) throws ServiceException {
        return exclude.extract(properties) ?
                twinFieldRepository.sumChildrenTwinFieldValuesWithStatusNotIn(twinFieldEntity.getTwin().getId(), childrenTwinClassFieldId.extract(properties), childrenTwinStatusIdList.extract(properties)) :
                twinFieldRepository.sumChildrenTwinFieldValuesWithStatusIn(twinFieldEntity.getTwin().getId(), childrenTwinClassFieldId.extract(properties), childrenTwinStatusIdList.extract(properties));
    }

}