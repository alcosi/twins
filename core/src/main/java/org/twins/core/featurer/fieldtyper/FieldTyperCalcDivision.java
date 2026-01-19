package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinFieldSimpleRepository;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageCalcDivision;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_1323, name = "Division", description = "First / Second")
public class FieldTyperCalcDivision extends FieldTyperCalcBinaryBase<TwinFieldStorageCalcDivision> {

    @FeaturerParam(name = "divisionByZeroResul", description = "Result if division by zero", defaultValue = "<n/a>")
    public static final FeaturerParamString divisionByZeroResul = new FeaturerParamString("divisionByZeroResul");

    @Autowired
    TwinFieldSimpleRepository twinFieldSimpleRepository;

    @Override
    public TwinFieldStorageCalcDivision getStorage(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException {
        return new TwinFieldStorageCalcDivision(
                twinFieldSimpleRepository,
                twinClassFieldEntity.getId(),
                firstFieldId.extract(properties),
                secondFieldId.extract(properties),
                divisionByZeroResul.extract(properties));
    }
}
