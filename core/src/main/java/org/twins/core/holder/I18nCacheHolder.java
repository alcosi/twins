package org.twins.core.holder;

import java.util.*;

public class I18nCacheHolder {
    public static final String PREFIX = "#i18n=";

    // i18n ids for load
    private static final ThreadLocal<Set<UUID>> i18nIds = ThreadLocal.withInitial(HashSet::new);

    // Map<UUID, translation> loaded translations
    private static final ThreadLocal<Map<UUID, String>> i18nTranslations = ThreadLocal.withInitial(HashMap::new);

    private static final ThreadLocal<Map<UUID, Map<String, String>>> i18nContexts = ThreadLocal.withInitial(HashMap::new);

    public static String addId(UUID id) {
        if (id != null) {
            i18nIds.get().add(id);
            return PREFIX + id;
        } else
            return "";

    }

    public static String addId(UUID id, Map<String, String> context) {
        String result = addId(id);
        if (id != null && context != null && !context.isEmpty()) {
            i18nContexts.get().put(id, new HashMap<>(context));
        }
        return result;
    }

    public static Set<UUID> getIdsToLoad() {
        return i18nIds.get();
    }

    public static Map<UUID, Map<String, String>> getContexts() {
        return i18nContexts.get();
    }

    public static void setTranslations(Map<UUID, String> cache) {
        i18nTranslations.set(cache);
    }

    public static Map<UUID, String> getTranslations() {
        return i18nTranslations.get();
    }

    public static void clear() {
        i18nIds.remove();
        i18nTranslations.remove();
        i18nContexts.remove();
    }
}