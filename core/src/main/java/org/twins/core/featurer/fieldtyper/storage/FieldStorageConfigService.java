package org.twins.core.featurer.fieldtyper.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class FieldStorageConfigService {
    private final Map<Class<?>, FieldStorageConfig> fieldStorageConfigNoPropertiesMap = new HashMap<>();
    @Autowired
    public void setFieldStorageList(List<TwinFieldStorage> storageList) {
        for (var storage : storageList) {
            fieldStorageConfigNoPropertiesMap.put(storage.getClass(), new FieldStorageConfig(storage, null));
        }
    }

    public FieldStorageConfig getConfig(Class<?> clazz) {
        return fieldStorageConfigNoPropertiesMap.get(clazz);
    }
}
