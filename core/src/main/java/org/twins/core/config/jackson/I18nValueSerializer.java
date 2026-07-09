package org.twins.core.config.jackson;

import org.springframework.boot.jackson.JacksonComponent;
import org.twins.core.holder.I18nCacheHolder;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

import java.util.Map;

@JacksonComponent
public class I18nValueSerializer extends ValueSerializer<String> {
    @Override
    public void serialize(String value, JsonGenerator gen, SerializationContext serializers) throws JacksonException {
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
