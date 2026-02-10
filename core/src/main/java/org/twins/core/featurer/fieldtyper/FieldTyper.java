package org.twins.core.featurer.fieldtyper;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.ValidationResult;
import org.cambium.common.exception.ErrorCode;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.domain.Specification;
import org.twins.core.dao.error.ErrorEntity;
import org.twins.core.dao.error.ErrorRepository;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldinitializer.FieldInitializer;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptor;
import org.twins.core.featurer.fieldtyper.storage.FieldStorageService;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorage;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.service.history.HistoryService;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassService;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;


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
    private ErrorRepository errorRepository;

    @Lazy
    @Autowired
    TwinService twinService;

    @Lazy
    @Autowired
    TwinClassService twinClassService;

    @Lazy
    @Autowired
    FieldStorageService fieldStorageService;

    private Class<T> valueType = null;
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
            if (FieldValue.class.isAssignableFrom(cl) && valueType == null)
                valueType = (Class<T>) cl;
            if (TwinFieldStorage.class.isAssignableFrom(cl) && storageType == null)
                storageType = (Class<S>) cl;
            if (TwinFieldSearch.class.isAssignableFrom(cl) && twinFieldSearchType == null)
                twinFieldSearchType = (Class<A>) cl;
        }
        if (descriptorType == null || valueType == null || storageType == null || twinFieldSearchType == null)
            throw new RuntimeException("Can not initialize ");
    }

    public Class<T> getValueType(TwinClassFieldEntity twinClassField) throws ServiceException {
        return valueType;
    }

    public Class<D> getFieldDescriptorType(TwinClassFieldEntity twinClassField) throws ServiceException {
        return descriptorType;
    }

    public Class<S> getStorageType() {
        return storageType;
    }

    public Class<A> getTwinFieldSearch() {
        return twinFieldSearchType;
    }

    public D getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity) throws ServiceException {
        var fieldTyperProperties = featurerService.extractProperties(this, twinClassFieldEntity.getFieldTyperParams());
        var descriptor = getFieldDescriptor(twinClassFieldEntity, fieldTyperProperties);
        var fieldInitializerProperties = featurerService.extractProperties(twinClassFieldEntity.getFieldInitializerFeaturerId(), twinClassFieldEntity.getFieldInitializerParams());
        var fieldInitializer = featurerService.getFeaturer(twinClassFieldEntity.getFieldInitializerFeaturerId(), FieldInitializer.class);
        fieldInitializer.appendDescriptor(fieldInitializerProperties, descriptor);
        return descriptor;
    }

    protected abstract D getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException;

    public void serializeValue(TwinEntity twin, T value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, value.getTwinClassField().getFieldTyperParams());
        if (value.isUndefined()) {
            //let's try to init field
            initializeField(twin, value);
        }
        if (value.isCleared()) {
            //todo some common clear logic
        } else {
            if (!validate(twin, value).isValid()) {
                throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT, "Can not serialize invalid value for " + value.getTwinClassField().logNormal());
            }
        }
        if (value.isUndefined()) {
            log.info("{} is undefined, serialization will be skipped", value.getTwinClassField().logNormal());
            return;
        }
        serializeValue(properties, twin, value, twinChangesCollector);
    }

    protected abstract void serializeValue(Properties properties, TwinEntity twin, T value, TwinChangesCollector twinChangesCollector) throws ServiceException;


    public T deserializeValue(TwinField twinField) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, twinField.getTwinClassField().getFieldTyperParams());
        return deserializeValue(properties, twinField);
    }

    protected abstract T deserializeValue(Properties properties, TwinField twinField) throws ServiceException;

    public Specification<TwinEntity> searchBy(A twinFieldSearch) throws ServiceException {
        throw new ServiceException(ErrorCodeTwins.FIELD_TYPER_SEARCH_NOT_IMPLEMENTED, "Field of type: [" + this.getClass().getSimpleName() + "] do not support twin field search not implemented");
    }

    public TwinFieldStorage getStorage(TwinClassFieldEntity twinClassFieldEntity) throws ServiceException {
        if (twinClassFieldEntity.getFieldStorage() == null) {
            Properties properties = featurerService.extractProperties(this, twinClassFieldEntity.getFieldTyperParams());
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

    public ValidationResult validate(TwinEntity twin, T value) throws ServiceException {
        if (value.isValidated()) { // already validated, no need to validate again
            return value.getValidationResult();
        }
        if (!valueType.isInstance(value)) {
            log.error("{} incorrect value type", value.getTwinClassField().logNormal());
            return new ValidationResult(false, getErrorMessage(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_TYPE_INCORRECT, value.getTwinClassField()));
        }
        //todo - correct after merging TWINS-418 branch (pluggable fields logic)
        if (!twinClassService.isInstanceOf(twin.getTwinClass(), value.getTwinClassField().getTwinClassId())) {
            log.error("{} is not suitable for {}", value.getTwinClassField().logNormal(), twin.logNormal());
            return new ValidationResult(false, getErrorMessage(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT, value.getTwinClassField()));
        }
        if (!twin.isSketch() // check required for non-sketch twins
                && value.getTwinClassField().getRequired()
                && value.isEmpty()) {
            log.error("{} is required", value.getTwinClassField().logNormal());
            return new ValidationResult(false, getErrorMessage(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED, value.getTwinClassField()));
        }
        Properties properties = featurerService.extractProperties(this, value.getTwinClassField().getFieldTyperParams());
        ValidationResult validationResult = validate(properties, twin, value);
        value.setValidationResult(validationResult);
        return validationResult;
    }

    public void initializeField(TwinEntity twin, T value) throws ServiceException {
        var fieldInitializer = featurerService.getFeaturer(value.getTwinClassField().getFieldInitializerFeaturerId(), FieldInitializer.class);
        fieldInitializer.setInitValue(twin, value);
    }

    /*
     * Override this method if you want to validate a field value.
     */
    protected ValidationResult validate(Properties properties, TwinEntity twin, T fieldValue) throws ServiceException {
        return new ValidationResult(true);
    }

    private String getErrorMessage(ErrorCode errorCode, TwinClassFieldEntity twinClassField) throws ServiceException {
        Hashtable<String, String> context = new Hashtable<>();
        context.put("field.name", i18nService.translateToLocale(twinClassField.getNameI18nId())); //todo think over batch load
        //todo change to errorService call also think over errorCode to twinClassField mapping
        ErrorEntity errorEntity = errorRepository.findByErrorCodeLocal(errorCode.getCode());
        if (errorEntity != null)
            return i18nService.translateToLocale(errorEntity.getClientMsgI18nId(), context);
        else
            return errorCode.getMessage();
    }
}
