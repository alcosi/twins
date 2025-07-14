package org.twins.core.featurer.fieldtyper.storage;

import lombok.Getter;
import lombok.Setter;
import org.cambium.common.kit.Kit;
import org.twins.core.dao.twin.TwinEntity;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public interface TwinFieldStorage {
    void load(Kit<TwinEntity, UUID> twinsKit);

    boolean hasStrictValues(UUID twinClassFieldId);

    boolean isLoaded(TwinEntity twinEntity);

    void initEmpty(TwinEntity twinEntity);

    @Getter
    @Setter
    class Config {
        Class<? extends TwinFieldStorage> storageClass;
        Map<String, String> properties;

        @Override
        public int hashCode() {
            return Objects.hash(storageClass, properties);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Config config = (Config) o;
            return Objects.equals(storageClass, config.storageClass) &&
                    Objects.equals(properties, config.properties);
        }

    }
}
