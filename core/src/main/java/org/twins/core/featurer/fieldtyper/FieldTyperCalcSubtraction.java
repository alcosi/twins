package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinFieldSimpleRepository;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageCalcSubtraction;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageSimple;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_1321, name = "Subtraction", description = "First - Second")
public class FieldTyperCalcSubtraction extends FieldTyperCalcBinaryBase<TwinFieldStorageSimple> {

    @Autowired
    TwinFieldSimpleRepository twinFieldSimpleRepository;

    @Override
    public TwinFieldStorageCalcSubtraction getStorage(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException {
        return new TwinFieldStorageCalcSubtraction(
                twinFieldSimpleRepository,
                twinClassFieldEntity.getId(),
                firstFieldId.extract(properties),
                secondFieldId.extract(properties));
    }

    @Override
    protected String calculate(Double v1, Double v2, Properties properties) throws ServiceException {
        double d1 = v1 != null ? v1 : 0.0;
        double d2 = v2 != null ? v2 : 0.0;
        return String.valueOf(d1 - d2);
    }
}
