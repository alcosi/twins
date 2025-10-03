package org.twins.core.featurer.fieldrule.fieldoverwriter;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.dao.twinclass.TwinClassFieldRuleEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldrule.conditionevaluator.conditiondescriptor.ConditionDescriptor;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptor;

import java.lang.reflect.Type;
import java.util.*;

@FeaturerType(id = FeaturerTwins.TYPE_46,
        name = "FieldOverwriter",
        description = "Overwrites value or params of twin class field after rules application")
@Slf4j
public abstract class FieldOverwriter <D extends FieldDescriptor> extends FeaturerTwins{
    private Class<D> descriptorType = null;

    public FieldOverwriter() {
        // Collect all parameterized types up the inheritance chain to detect concrete descriptor class
        List<Type> collected = collectParameterizedTypes(getClass(), new ArrayList<>());
        for (Type ptType : collected) {
            if (!(ptType instanceof Class<?> cl))
                continue;
            if (ConditionDescriptor.class.isAssignableFrom(cl) && descriptorType == null)
                //noinspection unchecked
                descriptorType = (Class<D>) cl;
        }
        if (descriptorType == null)
            throw new RuntimeException("Can not initialize FieldOverwriter: descriptor type not resolved for " + getClass().getSimpleName());
    }


    private static List<Type> collectParameterizedTypes(Class<?> _class, List<Type> collected) {
        Type t = _class.getGenericSuperclass();
        if (t instanceof java.lang.reflect.ParameterizedType pt) {
            collected.addAll(Arrays.asList(pt.getActualTypeArguments()));
        }
        if (_class.getSuperclass() == null)
            return collected;
        return collectParameterizedTypes(_class.getSuperclass(), collected);
    }

    public D getFieldOverwriterDescriptor(TwinClassFieldRuleEntity twinClassFieldRuleEntity) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, twinClassFieldRuleEntity.getFieldOverwriterParams(), new HashMap<>());
        return getFieldOverwriterDescriptor(twinClassFieldRuleEntity, properties);
    }

    protected abstract D getFieldOverwriterDescriptor(TwinClassFieldRuleEntity twinClassFieldRuleEntity, Properties properties) throws ServiceException;

}
