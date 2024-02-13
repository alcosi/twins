package org.twins.core.featurer.fieldtyper;

import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.twins.core.dao.twin.TwinFieldEntity;
import org.twins.core.exception.ErrorCodeTwins;

public interface FieldTyperCalcChildrenField {

    @FeaturerParam(name = "childrenTwinClassFieldId", description = "Twin.Class.Field Id of child twin fields")
    FeaturerParamUUID childrenTwinClassFieldId = new FeaturerParamUUID("childrenTwinClassFieldId");

    @FeaturerParam(name = "childrenTwinStatusIdList", description = "Twin.Status.IDs of child fields twin")
    FeaturerParamUUIDSet childrenTwinStatusIdList = new FeaturerParamUUIDSet("childrenTwinStatusIdList");

    @FeaturerParam(name = "exclude", description = "Exclude(true)/Include(false) child-field's Twin.Stauss.IDs from query result")
    FeaturerParamBoolean exclude = new FeaturerParamBoolean("exclude");

    default Double parseTwinFieldValue(TwinFieldEntity twinFieldEntity) throws ServiceException {
        double result = 0d;
        if(null != twinFieldEntity.getValue()){
            try {
                Double.parseDouble(twinFieldEntity.getValue());
            } catch (NumberFormatException e) {
                throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT, twinFieldEntity.easyLog(EasyLoggable.Level.NORMAL) + " value[" + twinFieldEntity.getValue() + "] cant be parsed to Double");
            }
        }
        return result;
    }

}
