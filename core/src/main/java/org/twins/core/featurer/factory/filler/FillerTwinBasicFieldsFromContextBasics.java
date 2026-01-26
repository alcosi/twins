package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.UuidUtils;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.TwinBasicFields;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamBasicsSetTwinBasicField;
import org.twins.core.service.user.UserService;

import java.util.*;

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
            Set<UUID> needLoad = new HashSet<>();
            Kit<UserEntity, UUID> loadedUsersKit;
            if (fieldsString.contains(TwinBasicFields.Basics.createdByUserId)) {
                if (UuidUtils.isNullifyMarker(basics.getCreatedByUserId())) {
                    outputTwinEntity.setCreatedByUserId(factoryItem.getOutput().nullifyUUID());
                } else {
                    outputTwinEntity.setCreatedByUserId(basics.getCreatedByUserId());
                    if (outputTwinEntity.getCreatedByUserId() != null && outputTwinEntity.getCreatedByUser() == null) {
                        needLoad.add(outputTwinEntity.getCreatedByUserId());
                    }
                }
            }
            if (fieldsString.contains(TwinBasicFields.Basics.assigneeUserId)) {
                if (UuidUtils.isNullifyMarker(basics.getAssigneeUserId())) {
                    outputTwinEntity.setAssignerUserId(factoryItem.getOutput().nullifyUUID());
                } else {
                    outputTwinEntity.setAssignerUserId(basics.getAssigneeUserId());
                    if (outputTwinEntity.getAssignerUser() == null) {
                        needLoad.add(outputTwinEntity.getAssignerUserId());
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(needLoad)) {
                loadedUsersKit = userService.findEntitiesSafe(needLoad);
                if (fieldsString.contains(TwinBasicFields.Basics.createdByUserId) && outputTwinEntity.getCreatedByUser() == null) {
                    outputTwinEntity.setCreatedByUser(loadedUsersKit.get(outputTwinEntity.getCreatedByUserId()));
                }
                if (fieldsString.contains(TwinBasicFields.Basics.assigneeUserId) && outputTwinEntity.getAssignerUser() == null) {
                    outputTwinEntity.setAssignerUser(loadedUsersKit.get(outputTwinEntity.getAssignerUserId()));
                }
            }
            if (fieldsString.contains(TwinBasicFields.Basics.name))
                outputTwinEntity.setName(basics.getName());
            if (fieldsString.contains(TwinBasicFields.Basics.description))
                outputTwinEntity.setDescription(basics.getDescription());
        }
    }
}
