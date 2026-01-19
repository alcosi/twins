package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinFieldSimpleRepository;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageCalcMultiplication;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_1322, name = "Multiplication", description = "First * Second")
public class FieldTyperCalcMultiplication extends FieldTyperCalcBinaryBase<TwinFieldStorageCalcMultiplication> {

    @FeaturerParam(name = "replaceZeroWithOne", description = "if some filed value is null or 0, then mulitply on 1")
    public static final FeaturerParamBoolean replaceZeroWithOne = new FeaturerParamBoolean("replaceZeroWithOne");

    @Autowired
    TwinFieldSimpleRepository twinFieldSimpleRepository;

    @Override
    public TwinFieldStorageCalcMultiplication getStorage(TwinClassFieldEntity twinClassFieldEntity,
            Properties properties) throws ServiceException {
        return new TwinFieldStorageCalcMultiplication(
                twinFieldSimpleRepository,
                twinClassFieldEntity.getId(),
                firstFieldId.extract(properties),
                secondFieldId.extract(properties),
                replaceZeroWithOne.extract(properties));
    }
}
