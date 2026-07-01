package org.twins.core.featurer.fieldtyper;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinFieldSimpleRepository;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchNotImplemented;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorText;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorage;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageCalcChildrenInStatusCount;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_1314,
        name = "Count children twins by child-status (on fly)",
        description = "Get count of child-twins by child-status(inc/exc) on fly")
@RequiredArgsConstructor
public class FieldTyperCountChildrenByStatusV1 extends FieldTyperCalcOnFly<FieldDescriptorText, FieldValueText, TwinFieldStorageCalcChildrenInStatusCount, TwinFieldSearchNotImplemented> implements FieldTyperCountChildrenByStatus {
    public static final Integer ID = 1314;

    private final TwinRepository twinRepository;

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
        var permissionContext = calcPermissionContext();
        return new TwinFieldStorageCalcChildrenInStatusCount(
                twinRepository,
                twinClassFieldEntity.getId(),
                childrenTwinStatusIdList.extract(properties),
                exclude.extract(properties),
                permissionContext.userId(),
                permissionContext.userGroupFootprintId());
    }
}
