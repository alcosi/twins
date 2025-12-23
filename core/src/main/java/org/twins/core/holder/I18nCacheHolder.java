package org.twins.core.holder;

import org.cambium.common.util.MapUtils;

import java.util.*;

public class I18nCacheHolder {
    public static final String PREFIX = "#i18n=";
    public static final String CONTEXT = ":context=";

    // i18n ids for load
    private static final ThreadLocal<Set<UUID>> i18nIds = ThreadLocal.withInitial(HashSet::new);

    // Map<String, translation> loaded translations
    // key - #i18n=<UUID> or #i18n=<UUID>:context=<context hash>
    private static final ThreadLocal<Map<String, String>> i18nTranslations = ThreadLocal.withInitial(HashMap::new);

    private static final ThreadLocal<I18nContextCache> i18nContexts = ThreadLocal.withInitial(I18nContextCache::new);

    public static String addId(UUID id) {
        if (id != null) {
            i18nIds.get().add(id);
            return generateKey(id, null);
        } else
            return "";

    }

    public static String addId(UUID id, Map<String, String> context) {
        if (id == null) {
            return "";
        }
        i18nIds.get().add(id);
        if (MapUtils.isNotEmpty(context)) {
            i18nContexts.get().putContext(id, context);
        }
        return generateKey(id, context);
    }

    public static String generateKey(UUID id, Map<String, String> context) {
        return PREFIX + id + (MapUtils.isNotEmpty(context) ? CONTEXT + context.hashCode() : "");
    }

    public static Set<UUID> getIdsToLoad() {
        return i18nIds.get();
    }

    public static I18nContextCache getContexts() {
        return i18nContexts.get();
    }

    public static void setTranslations(Map<String, String> cache) {
        i18nTranslations.set(cache);
    }

    public static Map<String, String> getTranslations() {
        return i18nTranslations.get();
    }

    public static void clear() {
        i18nIds.remove();
        i18nTranslations.remove();
        i18nContexts.remove();
    }

    public static class I18nContextCache extends HashMap<UUID, Set<Map<String, String>>>{
        public void putContext(UUID i18nId, Map<String, String> context) {
            computeIfAbsent(i18nId, k -> new HashSet<>()).add(context);
        }
    }
}