package org.twins.core.featurer.fieldtyper;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchNotImplemented;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorText;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorage;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageCalcBackwardLinkedTwinCount;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.service.auth.AuthService;

import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_1351,
        name = "Count linked twins by link and status (on fly)",
        description = "Get count of linked twins by link and status(inc/exc) on fly")
@RequiredArgsConstructor
public class FieldTyperCountChildrenByLinkV1 extends FieldTyperImmutable<FieldDescriptorText, FieldValueText, TwinFieldStorageCalcBackwardLinkedTwinCount, TwinFieldSearchNotImplemented> implements FieldTyperCountChildrenByLink {

    private final TwinRepository twinRepository;
    private final AuthService authService;

    @Deprecated
    @Override
    public FieldDescriptorText getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new FieldDescriptorText();
    }

    @Override
    protected FieldValueText deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        return new FieldValueText(twinField.getTwinClassField())
                .setValue(twinField.getTwin().getTwinFieldCalculated().get(twinField.getTwinClassFieldId()).toString());
    }

    @Override
    public TwinFieldStorage getStorage(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException {
        UUID userId = authService.getApiUser().getUserId();
        UUID userGroupFootprintId = authService.getApiUser().getUser().getUserGroupsFootprint();
        return new TwinFieldStorageCalcBackwardLinkedTwinCount(
                twinRepository,
                twinClassFieldEntity.getId(),
                linkIds.extract(properties),
                linkedTwinStatusIdList.extract(properties),
                exclude.extract(properties),
                userId,
                userGroupFootprintId);
    }
}
