package org.twins.core.featurer.twin.validator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.cambium.common.ValidationResult;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkRepository;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsStatusId;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsLinkId;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;

import java.util.Collection;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@Featurer(
        id = FeaturerTwins.ID_1621,
        name = "Twin has linked src twin field value greater than zero in statuses",
        description = "Checks that current twin as dst has at least one linked src twin in selected statuses with decimal field value greater than zero"
)
@RequiredArgsConstructor
public class TwinValidatorTwinChildrenFieldSumInStatusesPositive extends TwinValidator {
    @FeaturerParam(name = "Link id", description = "", order = 1)
    public static final FeaturerParamUUID linkId = new FeaturerParamUUIDTwinsLinkId("linkId");

    @FeaturerParam(name = "Status ids", description = "", order = 2)
    public static final FeaturerParamUUIDSet statusIds = new FeaturerParamUUIDSetTwinsStatusId("statusIds");

    @FeaturerParam(name = "Twin class field id", description = "", order = 3)
    public static final FeaturerParamUUID twinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("twinClassFieldId");

    private final TwinLinkRepository twinLinkRepository;

    @Override
    protected ValidationResult isValid(Properties properties, TwinEntity twinEntity, boolean invert) throws ServiceException {
        Set<UUID> extractedStatusIds = statusIds.extract(properties);
        UUID extractedLinkId = linkId.extract(properties);
        UUID extractedTwinClassFieldId = twinClassFieldId.extract(properties);
        if (twinEntity.getId() == null || extractedStatusIds == null || extractedStatusIds.isEmpty()) {
            return buildResult(
                    false,
                    invert,
                    twinEntity.logShort() + " has no id or empty statuses set",
                    twinEntity.logShort() + " has no id or empty statuses set"
            );
        }
        boolean isValid = twinLinkRepository.existsDstTwinLinkedFromSrcWithStatusAndPositiveDecimalField(
                twinEntity.getId(),
                extractedLinkId,
                extractedStatusIds,
                extractedTwinClassFieldId
        );
        return buildResult(
                isValid,
                invert,
                twinEntity.logShort() + " has no linked src twins by link[" + extractedLinkId + "] with field[" + extractedTwinClassFieldId + "] > 0 in statuses[" + StringUtils.join(extractedStatusIds, ",") + "]",
                twinEntity.logShort() + " has linked src twins by link[" + extractedLinkId + "] with field[" + extractedTwinClassFieldId + "] > 0 in statuses[" + StringUtils.join(extractedStatusIds, ",") + "]"
        );
    }

    @Override
    protected CollectionValidationResult isValid(Properties properties, Collection<TwinEntity> twinEntityCollection, boolean invert) throws ServiceException {
        Set<UUID> extractedStatusIds = statusIds.extract(properties);
        UUID extractedLinkId = linkId.extract(properties);
        UUID extractedTwinClassFieldId = twinClassFieldId.extract(properties);
        CollectionValidationResult result = new CollectionValidationResult();
        if (twinEntityCollection.isEmpty()) {
            return result;
        }

        Set<UUID> dstTwinIds = twinEntityCollection.stream()
                .map(TwinEntity::getId)
                .filter(id -> id != null)
                .collect(java.util.stream.Collectors.toSet());
        if (dstTwinIds.isEmpty() || extractedStatusIds == null || extractedStatusIds.isEmpty()) {
            for (TwinEntity twinEntity : twinEntityCollection) {
                result.getTwinsResults().put(twinEntity.getId(), buildResult(
                        false,
                        invert,
                        twinEntity.logShort() + " has no id or empty statuses set",
                        twinEntity.logShort() + " has no id or empty statuses set"
                ));
            }
            return result;
        }

        Set<UUID> matchedDstTwinIds = twinLinkRepository.findDstTwinIdsLinkedFromSrcWithStatusAndPositiveDecimalField(
                dstTwinIds,
                extractedLinkId,
                extractedStatusIds,
                extractedTwinClassFieldId
        );

        for (TwinEntity twinEntity : twinEntityCollection) {
            boolean isValid = twinEntity.getId() != null && matchedDstTwinIds.contains(twinEntity.getId());
            result.getTwinsResults().put(twinEntity.getId(), buildResult(
                    isValid,
                    invert,
                    twinEntity.logShort() + " has no linked src twins by link[" + extractedLinkId + "] with field[" + extractedTwinClassFieldId + "] > 0 in statuses[" + StringUtils.join(extractedStatusIds, ",") + "]",
                    twinEntity.logShort() + " has linked src twins by link[" + extractedLinkId + "] with field[" + extractedTwinClassFieldId + "] > 0 in statuses[" + StringUtils.join(extractedStatusIds, ",") + "]"
            ));
        }
        return result;
    }
}
