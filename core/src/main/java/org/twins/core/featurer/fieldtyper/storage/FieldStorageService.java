package org.twins.core.featurer.fieldtyper.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class FieldStorageService {
    private final Map<Class<?>, TwinFieldStorage> fieldStorageBeanMap = new HashMap<>();
    @Autowired
    public void setFieldStorageList(List<TwinFieldStorage> storageList) {
        for (var storage : storageList) {
            fieldStorageBeanMap.put(storage.getClass(), storage);
        }
    }

    public TwinFieldStorage getConfig(Class<?> clazz) {
        return fieldStorageBeanMap.get(clazz);
    }
}
