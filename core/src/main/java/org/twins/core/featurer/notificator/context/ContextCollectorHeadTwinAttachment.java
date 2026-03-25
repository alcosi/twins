package org.twins.core.featurer.notificator.context;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.stereotype.Component;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;
import org.twins.core.service.attachment.AttachmentService;
import org.twins.core.service.twin.TwinService;

import java.util.Map;
import java.util.Properties;
import java.util.UUID;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_4910,
        name = "Head Twin Attachment Context Collector",
        description = "Collects head twin attachment URL. Takes first attachment by order.")
@RequiredArgsConstructor
public class ContextCollectorHeadTwinAttachment extends ContextCollector {

    @FeaturerParam(name = "Collect attachment url key", description = "", order = 1, optional = true, defaultValue = "HEAD_TWIN_ATTACHMENT_URL")
    public static final FeaturerParamString collectKey = new FeaturerParamString("collectKey");

    @FeaturerParam(name = "Head twin class field id", description = "If specified, collects attachment from this field. Otherwise collects from head twin directly.", order = 2, optional = true)
    public static final FeaturerParamUUID headTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("headTwinClassFieldId");

    private final AttachmentService attachmentService;
    private final TwinService twinService;

    @Override
    protected Map<String, String> collectData(HistoryEntity history, Map<String, String> context, Properties properties) throws ServiceException {
        String key = collectKey.extract(properties);
        UUID fieldId = headTwinClassFieldId.extract(properties);

        twinService.loadHeadForTwin(history.getTwin());
        var headTwin = history.getTwin().getHeadTwin();

        if (headTwin != null) {
            TwinAttachmentEntity firstAttachment = attachmentService.findFirstAttachment(headTwin, fieldId);

            if (firstAttachment != null) {
                String url = attachmentService.getAttachmentUri(firstAttachment);
                if (url != null) {
                    context.put(key, url);
                }
            }
        }

        return context;
    }
}
