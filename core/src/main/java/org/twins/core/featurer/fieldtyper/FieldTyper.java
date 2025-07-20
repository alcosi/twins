package org.twins.core.featurer.fieldtyper;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptor;
import org.twins.core.featurer.fieldtyper.storage.FieldStorageService;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorage;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.service.history.HistoryService;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassService;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;


@FeaturerType(id = FeaturerTwins.TYPE_13,
        name = "FieldTyper",
        description = "Customize format of twin class field")
@Slf4j
public abstract class FieldTyper<D extends FieldDescriptor, T extends FieldValue, S extends TwinFieldStorage, A extends TwinFieldSearch> extends FeaturerTwins {
    @Lazy
    @Autowired
    HistoryService historyService;

    @Lazy
    @Autowired
    I18nService i18nService;

    @Lazy
    @Autowired
    TwinService twinService;

    @Lazy
    @Autowired
    TwinClassService twinClassService;

    @Lazy
    @Autowired
    FieldStorageService fieldStorageService;

    private Class<T> valuetype = null;
    private Class<D> descriptorType = null;
    private Class<S> storageType = null;
    private Class<A> twinFieldSearchType = null;

    public FieldTyper() {
        List<Type> collected = collectParameterizedTypes(getClass(), new ArrayList<>());
        for (Type ptType : collected) {
            if (!(ptType instanceof Class<?> cl))
                continue;
            if (FieldDescriptor.class.isAssignableFrom(cl) && descriptorType == null)
                descriptorType = (Class<D>) cl;
            if (FieldValue.class.isAssignableFrom(cl) && valuetype == null)
                valuetype = (Class<T>) cl;
            if (TwinFieldStorage.class.isAssignableFrom(cl) && storageType == null)
                storageType = (Class<S>) cl;
            if (TwinFieldSearch.class.isAssignableFrom(cl) && twinFieldSearchType == null)
                twinFieldSearchType = (Class<A>) cl;
        }
        if (descriptorType == null || valuetype == null || storageType == null || twinFieldSearchType == null)
            throw new RuntimeException("Can not initialize ");
    }

    public Class<T> getValueType() {
        return valuetype;
    }

    public Class<S> getStorageType() {
        return storageType;
    }

    public Class<A> getTwinFieldSearch() {
        return twinFieldSearchType;
    }

    private static List<Type> collectParameterizedTypes(Class<?> _class, List<Type> collected) {
        Type t = _class.getGenericSuperclass();
        if (t instanceof ParameterizedType pt) {
            collected.addAll(Arrays.asList(pt.getActualTypeArguments()));
        }
        if (_class.getSuperclass() == null)
            return collected;
        return collectParameterizedTypes(_class.getSuperclass(), collected);
    }

    public D getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, twinClassFieldEntity.getFieldTyperParams(), new HashMap<>());
        return getFieldDescriptor(twinClassFieldEntity, properties);
    }

    protected abstract D getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException;

    public void serializeValue(TwinEntity twin, T value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        if (!valuetype.isInstance(value)) {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_TYPE_INCORRECT);
        }
        if (!twinClassService.isInstanceOf(twin.getTwinClass(), value.getTwinClassField().getTwinClassId()))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT, value.getTwinClassField().logShort() + " is not suitable for " + twin.logNormal());
        Properties properties = featurerService.extractProperties(this, value.getTwinClassField().getFieldTyperParams(), new HashMap<>());
        serializeValue(properties, twin, value, twinChangesCollector);
    }

    protected abstract void serializeValue(Properties properties, TwinEntity twin, T value, TwinChangesCollector twinChangesCollector) throws ServiceException;


    public T deserializeValue(TwinField twinField) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, twinField.getTwinClassField().getFieldTyperParams(), new HashMap<>());
        return deserializeValue(properties, twinField);
    }

    protected abstract T deserializeValue(Properties properties, TwinField twinField) throws ServiceException;

    public Specification<TwinEntity> searchBy(A twinFieldSearch) throws ServiceException {
        throw new ServiceException(ErrorCodeTwins.FIELD_TYPER_SEARCH_NOT_IMPLEMENTED, "Field of type: [" + this.getClass().getSimpleName() + "] do not support twin field search not implemented");
    }

    public TwinFieldStorage getStorage(TwinClassFieldEntity twinClassFieldEntity) throws ServiceException {
        if (twinClassFieldEntity.getFieldStorage() == null) {
            Properties properties = featurerService.extractProperties(this, twinClassFieldEntity.getFieldTyperParams(), new HashMap<>());
            twinClassFieldEntity.setFieldStorage(getStorage(twinClassFieldEntity, properties));
        }
        return twinClassFieldEntity.getFieldStorage();
    }

    /**
     * Override this method only if fieldTyper has some load logic based on params.
     * In this case an individual storage config should be created.
     * Storages with the same hash codes will load data at once,
     * this helps to reduce db query count.
     *
     * @param twinClassFieldEntity
     * @param properties
     * @return
     */
    public TwinFieldStorage getStorage(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException {
        // This will return not null only if storage was configured as spring @component
        // and it was resolved by fieldStorageService. Otherwise, you need to override this method
        TwinFieldStorage twinFieldStorage = fieldStorageService.getConfig(getStorageType());
        if (twinFieldStorage == null) {
            throw new ServiceException(ErrorCodeTwins.FIELD_TYPER_STORAGE_NOT_INIT, "Storage: [" + getStorageType().getSimpleName() + "] is not a Spring @component and can not be resolved automatically. Please override getStorage() method in " + this.getClass().getSimpleName());
        }
        return twinFieldStorage;
    }
}
