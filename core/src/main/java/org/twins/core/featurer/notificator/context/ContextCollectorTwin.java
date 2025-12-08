package org.twins.core.featurer.notificator.context;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.annotations.FeaturerType;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassId;

import java.util.Map;
import java.util.Properties;

@FeaturerType(id = FeaturerTwins.ID_4902,
        name = "Context collector twin",
        description = "")
@Slf4j
public class ContextCollectorTwin extends ContextCollector {

    @FeaturerParam(name = "twin class id", description = "", order = 1)
    public static final FeaturerParamUUID twinClassId = new FeaturerParamUUIDTwinsTwinClassId("twinClassId");

    @FeaturerParam(name = "Collect name", description = "", order = 2)
    public static final FeaturerParamBoolean collectName = new FeaturerParamBoolean("collectName");

    @FeaturerParam(name = "Collect description", description = "", order = 3)
    public static final FeaturerParamBoolean collectDescription = new FeaturerParamBoolean("collectDescription");

    @Override
    protected Map<String, String> collect(HistoryEntity history, Map<String, String> context, Properties properties) throws ServiceException {
        TwinEntity twin = history.getTwin();
        //todo map param
        context.put("TWIN_ID", twin.getId().toString());
        if (collectName.extract(properties)) {
            String name = twin.getName();
            switch (classMap.get(twinClassId.extract(properties))) {
                case "PROJECT" -> context.put("PROJECT_NAME", name);
                case "TASK" -> context.put("TASK_NAME", name);
                case "SUBTASK" -> context.put("SUBTASK_NAME", name);
                default -> context.put("UNKNOWN_NAME", name);
            }
        }
        //todo impl description param
        return context;
    }
}
