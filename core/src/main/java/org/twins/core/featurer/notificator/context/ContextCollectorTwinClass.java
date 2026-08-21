package org.twins.core.featurer.notificator.context;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.stereotype.Component;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Map;
import java.util.Properties;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_4911,
        name = "Twin Class Context Collector",
        description = "Collects twin class information (id, key, name, description).")
@RequiredArgsConstructor
public class ContextCollectorTwinClass extends ContextCollector {

    @FeaturerParam(name = "Collect id", description = "", order = 1, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean collectId = new FeaturerParamBoolean("collectId");

    @FeaturerParam(name = "Collect id key", description = "", order = 2, optional = true, defaultValue = "TWIN_CLASS_ID")
    public static final FeaturerParamString collectIdKey = new FeaturerParamString("collectIdKey");

    @FeaturerParam(name = "Collect key", description = "", order = 3, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean collectKey = new FeaturerParamBoolean("collectKey");

    @FeaturerParam(name = "Collect key key", description = "", order = 4, optional = true, defaultValue = "TWIN_CLASS_KEY")
    public static final FeaturerParamString collectKeyKey = new FeaturerParamString("collectKeyKey");

    @FeaturerParam(name = "Collect name", description = "", order = 5, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean collectName = new FeaturerParamBoolean("collectName");

    @FeaturerParam(name = "Collect name key", description = "", order = 6, optional = true, defaultValue = "TWIN_CLASS_NAME")
    public static final FeaturerParamString collectNameKey = new FeaturerParamString("collectNameKey");

    @FeaturerParam(name = "Collect description", description = "", order = 7, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean collectDescription = new FeaturerParamBoolean("collectDescription");

    @FeaturerParam(name = "Collect description key", description = "", order = 8, optional = true, defaultValue = "TWIN_CLASS_DESCRIPTION")
    public static final FeaturerParamString collectDescriptionKey = new FeaturerParamString("collectDescriptionKey");

    /**
     * Overrides {@code collectDataBatch} directly (no {@link ContextCollectorAtomic}): i18n name/description
     * are registered via {@link ContextCollectorBatch#addI18n} (two-phase) so locale is resolved per-history
     * in bulk afterwards, not from a single thread-local {@code ApiUser}.
     */
    @Override
    public void collectDataBatch(ContextCollectorBatch batch, Properties properties) throws ServiceException {
        boolean doId = collectId.extract(properties);
        boolean doKey = collectKey.extract(properties);
        boolean doName = collectName.extract(properties);
        boolean doDescription = collectDescription.extract(properties);
        String idKey = collectIdKey.extract(properties);
        String keyKey = collectKeyKey.extract(properties);
        String nameKey = collectNameKey.extract(properties);
        String descriptionKey = collectDescriptionKey.extract(properties);
        for (var entry : batch.getContextByHistory().entrySet()) {
            HistoryEntity history = entry.getKey();
            Map<String, String> context = entry.getValue();
            var twin = history.getTwin();
            if (twin == null || twin.getTwinClass() == null) {
                continue; //todo logic if null
            }
            var twinClass = twin.getTwinClass();
            if (doId) {
                context.put(idKey, twinClass.getId().toString());
            }
            if (doKey) {
                context.put(keyKey, twinClass.getKey());
            }
            if (doName && twinClass.getNameI18NId() != null) {
                batch.addI18n(history, nameKey, twinClass.getNameI18NId());
            }
            if (doDescription && twinClass.getDescriptionI18NId() != null) {
                batch.addI18n(history, descriptionKey, twinClass.getDescriptionI18NId());
            }
        }
    }
}
