package org.twins.core.featurer.fieldrule.fieldoverwriter;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.dao.twinclass.TwinClassFieldRuleEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptor;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@FeaturerType(id = FeaturerTwins.TYPE_46,
        name = "FieldOverwriter",
        description = "Overwrites value or params of twin class field after rules application")
@Slf4j
public abstract class FieldParamOverwriter<D extends FieldDescriptor> extends FeaturerTwins{
    private Class<D> descriptorType = null;

    public FieldParamOverwriter() {
        // Collect all parameterized types up the inheritance chain to detect concrete descriptor class
        List<Type> collected = collectParameterizedTypes(getClass(), new ArrayList<>());
        for (Type ptType : collected) {
            if (!(ptType instanceof Class<?> cl))
                continue;
            if (FieldDescriptor.class.isAssignableFrom(cl) && descriptorType == null)
                //noinspection unchecked
                descriptorType = (Class<D>) cl;
        }
        if (descriptorType == null)
            throw new RuntimeException("Can not initialize FieldOverwriter: descriptor type not resolved for " + getClass().getSimpleName());
    }

    public D getFieldOverwriterDescriptor(TwinClassFieldRuleEntity twinClassFieldRuleEntity) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, twinClassFieldRuleEntity.getFieldOverwriterParams());
        return getFieldOverwriterDescriptor(twinClassFieldRuleEntity, properties);
    }

    protected abstract D getFieldOverwriterDescriptor(TwinClassFieldRuleEntity twinClassFieldRuleEntity, Properties properties) throws ServiceException;

}
