package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.TwinBasicFields;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamBasicsSetTwinBasicField;
import org.twins.core.service.user.UserService;

import java.util.Properties;
import java.util.Set;

@Component
@Featurer(id = FeaturerTwins.ID_2327,
        name = "Twin basic fields from context basics",
        description = "")
@Slf4j
public class FillerTwinBasicFieldsFromContextBasics extends Filler {

    @FeaturerParam(name = "Fields", description = "List of basic fields to fill", order = 1)
    public static final FeaturerParamBasicsSetTwinBasicField fields = new FeaturerParamBasicsSetTwinBasicField("fields");

    @Lazy
    @Autowired
    private UserService userService;

    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        TwinEntity outputTwinEntity = factoryItem.getOutput().getTwinEntity();
        TwinBasicFields basics = factoryItem.getFactoryContext().getBasics();
        Set<TwinBasicFields.Basics> fieldsString = fields.extract(properties);
        if (null != basics) {
            if (fieldsString.contains(TwinBasicFields.Basics.createdByUserId))
                outputTwinEntity
                        .setCreatedByUserId(basics.getCreatedByUserId())
                        .setCreatedByUser(userService.findEntitySafe(basics.getCreatedByUserId()));
            if (fieldsString.contains(TwinBasicFields.Basics.assigneeUserId))
                outputTwinEntity
                        .setAssignerUserId(basics.getAssigneeUserId())
                        .setAssignerUser(userService.findEntitySafe(basics.getAssigneeUserId()));;
            if (fieldsString.contains(TwinBasicFields.Basics.name))
                outputTwinEntity.setName(basics.getName());
            if (fieldsString.contains(TwinBasicFields.Basics.description))
                outputTwinEntity.setDescription(basics.getDescription());
        }
    }
}
