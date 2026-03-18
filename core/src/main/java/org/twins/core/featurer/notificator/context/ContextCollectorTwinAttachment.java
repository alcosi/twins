package org.twins.core.featurer.notificator.context;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.twins.core.dao.attachment.TwinAttachmentEntity;
import org.twins.core.dao.history.HistoryEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;
import org.twins.core.service.attachment.AttachmentService;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

@Slf4j
@Featurer(id = FeaturerTwins.ID_4707,
        name = "Twin Attachment Context Collector",
        description = "Collects twin attachment URL. Takes first attachment by order.")
@RequiredArgsConstructor
public class ContextCollectorTwinAttachment extends ContextCollector {

    @FeaturerParam(name = "Collect attachment url key", description = "", order = 1, optional = true, defaultValue = "TWIN_ATTACHMENT_URL")
    public static final FeaturerParamString collectKey = new FeaturerParamString("collectKey");

    @FeaturerParam(name = "Twin class field id", description = "If specified, collects attachment from this field. Otherwise collects from twin directly.", order = 2, optional = true)
    public static final FeaturerParamUUID twinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("twinClassFieldId");

    private final AttachmentService attachmentService;

    @Override
    protected Map<String, String> collectData(HistoryEntity history, Map<String, String> context, Properties properties) throws ServiceException {
        String key = collectKey.extract(properties);
        UUID fieldId = twinClassFieldId.extract(properties);
        UUID twinId = history.getTwin().getId();

        List<TwinAttachmentEntity> attachments = attachmentService.findAttachmentByTwinId(twinId);

        if (CollectionUtils.isEmpty(attachments)) {
            return context;
        }

        TwinAttachmentEntity firstAttachment = attachments.stream()
                .filter(a -> {
                    if (fieldId == null) {
                        // Direct twin attachments only (not from field, comment or transition)
                        return a.getTwinClassFieldId() == null
                                && a.getTwinCommentId() == null
                                && a.getTwinflowTransitionId() == null;
                    } else {
                        // Attachments from specific field
                        return fieldId.equals(a.getTwinClassFieldId());
                    }
                })
                .min(Comparator.comparingInt(a -> a.getOrder() != null ? a.getOrder() : Integer.MAX_VALUE))
                .orElse(null);

        if (firstAttachment != null) {
            String url = attachmentService.getAttachmentUri(firstAttachment);
            if (url != null) {
                context.put(key, url);
            }
        }

        return context;
    }
}
