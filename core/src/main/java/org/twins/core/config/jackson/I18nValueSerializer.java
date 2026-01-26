package org.twins.core.config.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.twins.core.holder.I18nCacheHolder;

import java.io.IOException;
import java.util.Map;

public class I18nValueSerializer extends JsonSerializer<String> {
    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value != null && value.startsWith(I18nCacheHolder.PREFIX)) {
            try {
                Map<String, String> cache = I18nCacheHolder.getTranslations();
                String translation = (cache != null) ? cache.get(value) : "";
                gen.writeString(translation != null ? translation : "");
                return;
            } catch (IllegalArgumentException ignored) {
                // not UUID
            }
        }
        gen.writeString(value);
    }
}