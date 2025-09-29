package org.twins.core.featurer.twin.validator;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.ValidationResult;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.twin.TwinSearchService;

import java.util.Properties;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_1608,
        name = "Count of twins same twin class GTE value",
        description = "Count twins with twin-class same to input twin and compare with GTEvalue(great or equals)")
public class TwinValidatorCountOfTwinsSameTwinClassGTEValue extends TwinValidator {

    @FeaturerParam(name = "GTE value", description = "count of twins must be great or equals to this value", order = 1)
    public static final FeaturerParamInt GTEvalue = new FeaturerParamInt("GTEvalue");

    @Lazy
    @Autowired
    TwinSearchService twinSearchService;

    @Override
    protected ValidationResult isValid(Properties properties, TwinEntity twinEntity, boolean invert) throws ServiceException {
        Integer val = GTEvalue.extract(properties);
        BasicSearch search = new BasicSearch();
        search
                .addTwinClassId(twinEntity.getTwinClassId(), false);
        long count = twinSearchService.count(search);
        boolean isValid = count >= val;
        return buildResult(
                isValid,
                invert,
                twinEntity.logShort() + " count=" + count + "(NOT >=" + val + ")",
                twinEntity.logShort() + " count=" + count + "(>=" + val + ")");
    }

}
