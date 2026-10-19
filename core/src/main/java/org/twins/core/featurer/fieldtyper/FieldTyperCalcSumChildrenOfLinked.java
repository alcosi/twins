package org.twins.core.featurer.fieldtyper;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinFieldDecimalRepository;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchNotImplemented;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorText;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorage;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageCalcSumChildrenOfLinked;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsClassId;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsStatusId;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsTwinClassFieldId;

import java.util.Properties;

/**
 * On-fly: for each context twin, follow links to related twins,
 * then sum decimal fields of <em>their</em> children, optionally
 * filtered by child class and/or status.
 */
@Component
@RequiredArgsConstructor
@Featurer(
        id = FeaturerTwins.ID_1357,
        name = "Sum children fields of linked twins",
        description = "Sum decimal fields of children of linked twins (on fly)"
)
public class FieldTyperCalcSumChildrenOfLinked
        extends FieldTyperImmutable<FieldDescriptorText, FieldValueText, TwinFieldStorageCalcSumChildrenOfLinked, TwinFieldSearchNotImplemented>
        implements FieldTyperCalcByLink {

    private final TwinFieldDecimalRepository twinFieldDecimalRepository;

    @FeaturerParam(name = "fieldIds", description = "Child twin fields to sum")
    public static final FeaturerParamUUIDSetTwinsTwinClassFieldId fieldIds = new FeaturerParamUUIDSetTwinsTwinClassFieldId("fieldIds");
    @FeaturerParam(name = "Children twin of class ids", order = 10, optional = true)
    public static final FeaturerParamUUIDSet childrenTwinOfClassIds = new FeaturerParamUUIDSetTwinsClassId("childrenTwinOfClassIds");
    @FeaturerParam(name = "Children twin in status ids", order = 11, optional = true)
    public static final FeaturerParamUUIDSet childrenTwinInStatusIds = new FeaturerParamUUIDSetTwinsStatusId("childrenTwinInStatusIds");
    @FeaturerParam(name = "Children status exclude", order = 12, optional = true, defaultValue = "false",
            description = "Exclude(true)/Include(false) childrenTwinInStatusIds from children query")
    public static final FeaturerParamBoolean childrenStatusExclude = new FeaturerParamBoolean("childrenStatusExclude");

    @Override
    protected FieldDescriptorText getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException {
        return new FieldDescriptorText();
    }

    @Override
    protected FieldValueText deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        return new FieldValueText(twinField.getTwinClassField())
                .setValue(String.valueOf(twinField.getTwin().getTwinFieldCalculated().get(twinField.getTwinClassFieldId())));
    }

    @Override
    public TwinFieldStorage getStorage(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new TwinFieldStorageCalcSumChildrenOfLinked(
                twinClassFieldEntity.getId(),
                twinFieldDecimalRepository,
                fieldIds.extract(properties),
                linkIds.extract(properties),
                srcElseDst.extract(properties),
                linkedTwinInStatusIdSet.extract(properties),
                linkedTwinOfClassIds.extract(properties),
                statusExclude.extract(properties),
                childrenTwinOfClassIds.extract(properties),
                childrenTwinInStatusIds.extract(properties),
                Boolean.TRUE.equals(childrenStatusExclude.extract(properties))
        );
    }
}
