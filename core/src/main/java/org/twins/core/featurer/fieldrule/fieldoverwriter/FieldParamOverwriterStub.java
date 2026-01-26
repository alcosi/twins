package org.twins.core.featurer.fieldrule.fieldoverwriter;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassFieldRuleEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptor;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_4601,
        name = "Field Overwriter Stub",
        description = "Does nothing")

public class FieldParamOverwriterStub extends FieldParamOverwriter<FieldDescriptor> {
    @Override
    protected FieldDescriptor getFieldOverwriterDescriptor(TwinClassFieldRuleEntity twinClassFieldRuleEntity, Properties properties) throws ServiceException {
        return null;
    }
}
