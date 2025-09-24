package org.twins.core.featurer.conditionevaluator;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.dao.twinclass.TwinClassFieldConditionEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.conditionevaluator.conditiondescriptor.ConditionDescriptor;

import java.lang.reflect.Type;
import java.util.*;

@FeaturerType(id = FeaturerTwins.TYPE_45,
        name = "ConditionEvaluator",
        description = "Evaluates conditions on twin class fields")
@Slf4j
public abstract class ConditionEvaluator<D extends ConditionDescriptor> extends FeaturerTwins {
    private Class<D> descriptorType = null;

   public ConditionEvaluator() {
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
            throw new RuntimeException("Can not initialize ConditionEvaluator: descriptor type not resolved for " + getClass().getSimpleName());
    }

    public Class<D> getDescriptorType() {
        return descriptorType;
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

    public D getConditionDescriptor(TwinClassFieldConditionEntity twinClassFieldConditionEntity) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, twinClassFieldConditionEntity.getConditionEvaluatorParams(), new HashMap<>());
        return getConditionDescriptor(twinClassFieldConditionEntity, properties);
    }

    protected abstract D getConditionDescriptor(TwinClassFieldConditionEntity twinClassFieldConditionEntity, Properties properties) throws ServiceException;

}
