package org.twins.core.unit.featurer.fieldinitializer;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValueSelect;
import org.twins.core.service.datalist.DataListOptionService;
import org.twins.core.service.datalist.DataListService;
import org.twins.core.featurer.fieldinitializer.FieldInitializerListDefaultOrNull;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.*;

class FieldInitializerListDefaultOrNullTest extends BaseUnitTest {

    @Mock
    private DataListService dataListService;

    @Mock
    private DataListOptionService dataListOptionService;

    @Mock
    private FeaturerService featurerService;

    private FieldInitializerListDefaultOrNull initializer;

    @BeforeEach
    void setUp() throws Exception {
        initializer = new FieldInitializerListDefaultOrNull();
        setField(initializer, "dataListService", dataListService);
        setField(initializer, "dataListOptionService", dataListOptionService);
        setField(initializer, "featurerService", featurerService);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        var field = findField(target.getClass(), fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private Field findField(Class<?> clazz, String fieldName) {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new RuntimeException("Field not found: " + fieldName);
    }

    private TwinClassFieldEntity buildTwinClassField(Integer featurerId, HashMap<String, String> typerParams) {
        var field = new TwinClassFieldEntity();
        field.setId(UUID.randomUUID());
        field.setKey("listField");
        field.setFieldTyperFeaturerId(featurerId);
        field.setFieldTyperParams(typerParams);
        return field;
    }
}
