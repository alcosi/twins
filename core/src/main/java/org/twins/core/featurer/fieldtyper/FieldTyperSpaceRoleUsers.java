package org.twins.core.featurer.fieldtyper;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.space.SpaceRoleUserEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.user.UserRepository;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorUser;
import org.twins.core.featurer.fieldtyper.value.FieldValueUser;
import org.twins.core.service.space.SpaceUserRoleService;

import java.util.List;
import java.util.Properties;

@Slf4j
@Component
@Lazy
@Featurer(id = 1318,
        name = "FieldTyperUser",
        description = "")
public class FieldTyperSpaceRoleUsers extends FieldTyper<FieldDescriptorUser, FieldValueUser, SpaceRoleUserEntity>{
    @Lazy
    @Autowired
    SpaceUserRoleService spaceUserRoleService;

    @Autowired
    UserRepository userRepository;

    @FeaturerParam(name = "userFilterUUID", description = "")
    public static final FeaturerParamUUID userFilterUUID = new FeaturerParamUUID("userFilterUUID");

    @FeaturerParam(name = "spaceRoleId", description = "")
    public static final FeaturerParamInt spaceRoleId = new FeaturerParamInt("spaceRoleId");

    @FeaturerParam(name = "longListThreshold", description = "If options count is bigger then given threshold longList type will be used")
    public static final FeaturerParamInt longListThreshold = new FeaturerParamInt("longListThreshold");

    @Override
    protected FieldDescriptorUser getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException {
        return null;
    }

    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, FieldValueUser value, TwinChangesCollector twinChangesCollector) throws ServiceException {
//        if (twin.getTwinClass().isPermissionSchemaSpace()) {
//            return;
//        }
        List<UserEntity> selectedUserEntityList = userRepository.findByIdIn(value.getUsers().stream().map(UserEntity::getId).toList());
//        spaceUserRoleService.overrideUsers(twin.getId(), spaceRoleId, );
    }

    @Override
    protected FieldValueUser deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        return null;
    }
}
