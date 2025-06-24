package org.twins.core.featurer.fieldtyper;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.StringUtils;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.twins.core.dao.space.SpaceRoleUserEntity;
import org.twins.core.dao.specifications.twin.TwinSpecification;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchSpaceRoleUser;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorUser;
import org.twins.core.featurer.fieldtyper.value.FieldValueUser;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.space.SpaceUserRoleService;
import org.twins.core.service.user.UserFilterService;
import org.twins.core.service.user.UserService;

import java.util.*;

@Slf4j
@Component
@Lazy
@Featurer(id = FeaturerTwins.ID_1332,
        name = "Space role users",
        description = "")
public class FieldTyperSpaceRoleUsers extends FieldTyper<FieldDescriptorUser, FieldValueUser, SpaceRoleUserEntity, TwinFieldSearchSpaceRoleUser> implements LongList {
    @Autowired
    @Lazy
    UserFilterService userFilterService;
    @Autowired
    UserService userService;

    @FeaturerParam(name = "User filter UUID", description = "", order = 1)
    public static final FeaturerParamUUID userFilterUUID = new FeaturerParamUUID("userFilterUUID");
    @FeaturerParam(name = "Space role id", description = "Space role id (member and etc.)", order = 2)
    public static final FeaturerParamUUID spaceRoleId = new FeaturerParamUUID("spaceRoleId");
    @FeaturerParam(name = "Long list threshold", description = "If options count is bigger then given threshold longList type will be used", order = 3)
    public static final FeaturerParamInt longListThreshold = new FeaturerParamInt("longListThreshold");
    @Autowired
    private SpaceUserRoleService spaceUserRoleService;
    @Autowired
    private AuthService authService;

    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, FieldValueUser value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        if (value.getTwinClassField().getRequired() && CollectionUtils.isEmpty(value.getUsers()))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED, value.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) + " is required");
        if (!value.getTwinClassField().getTwinClass().getPermissionSchemaSpace()) {
            return;
        }
        UUID userFilterId = userFilterUUID.extract(properties); //todo not implemented yet
        ApiUser apiUser = authService.getApiUser();
        UUID roleId = spaceRoleId.extract(properties);

        ImmutablePair<Set<UUID>, Set<UUID>> spaceRoleUserSets = spaceUserRoleService
                .calculateSpaceRoleUserChanges(twin.getId(), roleId, value.getUsers().stream().map(UserEntity::getId).toList());
        Set<UUID> collectionToAdd = spaceRoleUserSets.getLeft();
        Set<UUID> collectionToDelete = spaceRoleUserSets.getRight();

        if (CollectionUtils.isNotEmpty(collectionToAdd)) {
            List<UUID> invalidUsers = userService.getUsersOutOfDomainAndBusinessAccount(collectionToAdd, apiUser.getBusinessAccountId(), apiUser.getDomainId());
            if (CollectionUtils.isNotEmpty(invalidUsers)) {
                throw new ServiceException(ErrorCodeTwins.USER_UNKNOWN, "Users[" + StringUtils.join(invalidUsers, ",") + "] can not be added because they are out of current BA or Domain");
            }
            List<SpaceRoleUserEntity> listToAdd = new ArrayList<>();
            for (UUID userId : collectionToAdd) {
                listToAdd.add(new SpaceRoleUserEntity()
                        .setSpaceRoleId(roleId)
                        .setUserId(userId)
                        .setTwinId(twin.getId())
                        .setCreatedByUserId(apiUser.getUserId())
                );
            }
            twinChangesCollector.addAll(listToAdd);
        }
        if (CollectionUtils.isNotEmpty(collectionToDelete)) {
            spaceUserRoleService.deleteUsersFromSpaceRole(twin.getId(), roleId, collectionToDelete);
        }
    }

    @Override
    public FieldDescriptorUser getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException {
        UUID userFilterId = userFilterUUID.extract(properties);
        int listSize = userFilterService.countFilterResult(userFilterId);
        FieldDescriptorUser fieldDescriptorUser = new FieldDescriptorUser();
        if (listSize > getLongListThreshold(properties))
            fieldDescriptorUser.userFilterId(userFilterId);
        else {
            fieldDescriptorUser.validUsers(userFilterService.findUsers(userFilterId));
        }
        return fieldDescriptorUser;
    }

    @Override
    protected FieldValueUser deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        TwinEntity twinEntity = twinField.getTwin();
        twinService.loadTwinFields(twinEntity);
        UUID roleId = FieldTyperSpaceRoleUsers.spaceRoleId.extract(properties);
        List<UserEntity> spaceRoleUserEntityList = spaceUserRoleService.findUserBySpaceIdAndRoleId(twinEntity.getId(), roleId);
        FieldValueUser ret = new FieldValueUser(twinField.getTwinClassField());
        if (spaceRoleUserEntityList != null)
            for (UserEntity user : spaceRoleUserEntityList) {
                ret.add(user);
            }
        return ret;
    }

    @Override
    public int getLongListThreshold(Properties properties) {
        return longListThreshold.extract(properties);
    }

    @Override
    public Specification<TwinEntity> searchBy(TwinFieldSearchSpaceRoleUser search) {
        return Specification.where(TwinSpecification.checkSpaceRoleUser(search));
    }
}
