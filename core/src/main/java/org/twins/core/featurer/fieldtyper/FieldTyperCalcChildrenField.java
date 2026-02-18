package org.twins.core.featurer.fieldtyper;

import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldDecimalRepository;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsStatusId;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;

import java.math.BigDecimal;
import java.util.Properties;

public interface FieldTyperCalcChildrenField {
    @FeaturerParam(name = "childrenTwinClassFieldId", description = "Twin.Class.Field Id of child twin fields")
    FeaturerParamUUID childrenTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("childrenTwinClassFieldId");

    @FeaturerParam(name = "childrenTwinStatusIdList", description = "Twin.Status.IDs of child twin")
    FeaturerParamUUIDSet childrenTwinStatusIdList = new FeaturerParamUUIDSetTwinsStatusId("childrenTwinStatusIdList");

    @FeaturerParam(name = "exclude", description = "Exclude(true)/Include(false) child-field's Twin.Status.IDs from query result")
    FeaturerParamBoolean exclude = new FeaturerParamBoolean("exclude");

    default BigDecimal getSumResult(Properties properties, TwinEntity twinEntity, TwinFieldDecimalRepository twinFieldDecimalRepository) {
        return exclude.extract(properties) ?
                twinFieldDecimalRepository.sumChildrenTwinFieldValuesWithStatusNotIn(twinEntity.getId(), childrenTwinClassFieldId.extract(properties), childrenTwinStatusIdList.extract(properties)) :
                twinFieldDecimalRepository.sumChildrenTwinFieldValuesWithStatusIn(twinEntity.getId(), childrenTwinClassFieldId.extract(properties), childrenTwinStatusIdList.extract(properties));
    }

}
