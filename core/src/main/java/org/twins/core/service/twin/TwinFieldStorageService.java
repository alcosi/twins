package org.twins.core.service.twin;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorage;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class TwinFieldStorageService {
    @Getter
    private final Map<Class<?>, TwinFieldStorage> fieldStorageMap = new HashMap<>();
    @Autowired
    public void setFieldStorageList(List<TwinFieldStorage> storageList) {
        for (var storage : storageList) {
            fieldStorageMap.put(storage.getClass(), storage);
        }
    }

    public TwinFieldStorage getFieldStorage(Class<?> clazz) {
        return fieldStorageMap.get(clazz);
    }

    public Collection<TwinFieldStorage> getAllFieldStorages() {
        return fieldStorageMap.values();
    }
}
