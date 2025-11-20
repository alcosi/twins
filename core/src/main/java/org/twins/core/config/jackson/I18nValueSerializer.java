package org.twins.core.config.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.twins.core.holder.I18nCacheHolder;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class I18nValueSerializer extends JsonSerializer<String> {
    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value != null && value.startsWith(I18nCacheHolder.PREFIX)) {
            String uuidStr = value.substring(I18nCacheHolder.PREFIX.length());
            try {
                UUID uuid = UUID.fromString(uuidStr);
                Map<UUID, String> cache = I18nCacheHolder.getTranslations();
                String translation = (cache != null) ? cache.get(uuid) : null;
                gen.writeString(translation != null ? translation : "");
                return;
            } catch (IllegalArgumentException ignored) {
                // not UUID
            }
        }
        gen.writeString(value);
    }
}