package org.twins.core.featurer.fieldtyper;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinFieldSimpleEntity;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchNotImplemented;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorText;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.service.auth.AuthService;

import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_1315,
        name = "Count children twins",
        description = "Save count of child-twin by child-status(exl/inc) on serializeValue, and return saved total from database")
@RequiredArgsConstructor
public class FieldTyperCountChildrenByStatusV2 extends FieldTyperSimple<FieldDescriptorText, FieldValueText, TwinFieldSearchNotImplemented> implements FieldTyperCountChildrenByStatus {
    public static final Integer ID = 1315;

    private final TwinRepository twinRepository;
    private final AuthService authService;

    @Override
    public FieldDescriptorText getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new FieldDescriptorText();
    }

    @Override
    protected void serializeValue(Properties properties, TwinFieldSimpleEntity twinFieldEntity, FieldValueText value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        UUID userId = authService.getApiUser().getUserId();
        UUID userGroupFootprintId = authService.getApiUser().getUser().getUserGroupsFootprint();
        detectValueChange(twinFieldEntity, twinChangesCollector, getCountResult(properties, twinFieldEntity.getTwin(), twinRepository, userId, userGroupFootprintId).toString());
    }

    @Override
    protected FieldValueText deserializeValue(Properties properties, TwinField twinField, TwinFieldSimpleEntity twinFieldEntity) throws ServiceException {
        return new FieldValueText(twinField.getTwinClassField())
                .setValue(parseTwinFieldValue(twinFieldEntity).toString());
    }
}
