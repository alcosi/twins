package org.twins.core.featurer.fieldtyper;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.MapUtils;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamInt;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.history.context.HistoryContextUserMultiChange;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldUserEntity;
import org.twins.core.dao.twin.TwinFieldUserRepository;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dao.user.UserRepository;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorUser;
import org.twins.core.featurer.fieldtyper.value.FieldValueUser;
import org.twins.core.service.history.HistoryItem;
import org.twins.core.service.user.UserFilterService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@Lazy
@Featurer(id = 1311,
        name = "FieldTyperUser",
        description = "")
public class FieldTyperUser extends FieldTyper<FieldDescriptorUser, FieldValueUser, TwinFieldUserEntity> implements LongList {
    @Autowired
    @Lazy
    UserFilterService userFilterService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TwinFieldUserRepository twinFieldUserRepository;

    @FeaturerParam(name = "userFilterUUID", description = "")
    public static final FeaturerParamUUID userFilterUUID = new FeaturerParamUUID("userFilterUUID"); //todo change type

    @FeaturerParam(name = "multiple", description = "If true, then multiple select available")
    public static final FeaturerParamBoolean multiple = new FeaturerParamBoolean("multiple");

    @FeaturerParam(name = "longListThreshold", description = "If options count is bigger then given threshold longList type will be used")
    public static final FeaturerParamInt longListThreshold = new FeaturerParamInt("longListThreshold");

    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, FieldValueUser value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        if (value.getTwinClassField().isRequired() && CollectionUtils.isEmpty(value.getUsers()))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED, value.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) + " is required");
        if (value.getUsers() != null && value.getUsers().size() > 1 && !allowMultiply(properties))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_MULTIPLY_OPTIONS_ARE_NOT_ALLOWED, value.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) + " multiply options are not allowed");
        UUID userFilterId = userFilterUUID.extract(properties); //todo not implemented yet
        List<UserEntity> selectedUserEntityList = userRepository.findByIdIn(value.getUsers().stream().map(UserEntity::getId).toList());
        twinService.loadTwinFields(twin);
        Map<UUID, TwinFieldUserEntity> storedFieldUsers = null;
        if (twin.getTwinFieldUserKit().containsGroupedKey(value.getTwinClassField().getId()))
            storedFieldUsers = twin.getTwinFieldUserKit().getGrouped(value.getTwinClassField().getId()).stream().collect(Collectors.toMap(TwinFieldUserEntity::getUserId, Function.identity()));
        if (FieldValueChangeHelper.isSingleValueAdd(selectedUserEntityList, storedFieldUsers)) {
            UserEntity userEntity = selectedUserEntityList.get(0);
            twinChangesCollector.getHistoryCollector(twin).add(historyService.fieldChangeUser(value.getTwinClassField(), null, userEntity));
            twinChangesCollector.add(new TwinFieldUserEntity()
                    .setTwin(twin)
                    .setTwinId(twin.getId())
                    .setTwinClassFieldId(value.getTwinClassField().getId())
                    .setUserId(checkUserAllowed(twin, value.getTwinClassField(), userEntity))
                    .setUser(userEntity));
            return;
        }
        if (FieldValueChangeHelper.isSingleToSingleValueUpdate(selectedUserEntityList, storedFieldUsers)) {
            UserEntity userEntity = selectedUserEntityList.get(0);
            TwinFieldUserEntity storeField = MapUtils.pullAny(storedFieldUsers);
            if (!storeField.getUserId().equals(userEntity.getId())) {
                twinChangesCollector.getHistoryCollector(twin).add(historyService.fieldChangeUser(value.getTwinClassField(), storeField.getUser(), userEntity));
                twinChangesCollector.add(storeField //we can update existing record
                        .setUserId(checkUserAllowed(twin, value.getTwinClassField(), userEntity))
                        .setUser(userEntity));
            }
            return;
        }

        HistoryItem<HistoryContextUserMultiChange> historyItem = historyService.fieldChangeUserMulti(value.getTwinClassField());
        for (UserEntity userEntity : selectedUserEntityList) {
            //todo check if user valid for current filter result
            if (FieldValueChangeHelper.notSaved(userEntity.getId(), storedFieldUsers)) { // no values were saved before
                historyItem.getContext().shotAddedUserId(userEntity.getId());
                twinChangesCollector.add(new TwinFieldUserEntity()
                        .setTwin(twin)
                        .setTwinId(twin.getId())
                        .setTwinClassFieldId(value.getTwinClassField().getId())
                        .setUserId(checkUserAllowed(twin, value.getTwinClassField(), userEntity))
                        .setUser(userEntity));
            } else {
                storedFieldUsers.remove(userEntity.getId()); // we remove is from list, because all remained list elements will be deleted from database (pretty logic inversion)
            }
        }
        if (FieldValueChangeHelper.hasOutOfDateValues(storedFieldUsers)) {// old values must be deleted
            List<UUID> deletedUserIdList = new ArrayList<>();
            for (TwinFieldUserEntity deleteField : storedFieldUsers.values()) {
                deletedUserIdList.add(deleteField.getId()); // we have to delete fields by id, not by userId
                historyItem.getContext().shotDeletedUserId(deleteField.getUserId());
            }
            twinChangesCollector.deleteAll(TwinFieldUserEntity.class, deletedUserIdList);
        }
        if (historyItem.getContext().notEmpty())
            twinChangesCollector.getHistoryCollector(twin).add(historyItem);
    }

    public UUID checkUserAllowed(TwinEntity twinEntity, TwinClassFieldEntity twinClassFieldEntity, UserEntity userEntity) throws ServiceException {
        return userEntity.getId(); // can be ovмrrided in case if value must be shared between twins
    }

    @Override
    public FieldDescriptorUser getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException {
        UUID userFilterId = userFilterUUID.extract(properties);
        int listSize = userFilterService.countFilterResult(userFilterId);
        FieldDescriptorUser fieldDescriptorUser = new FieldDescriptorUser()
                .multiple(multiple.extract(properties));
        if (listSize > getLongListThreshold(properties))
            fieldDescriptorUser.userFilterId(userFilterId);
        else {
            fieldDescriptorUser.validUsers(userFilterService.findUsers(userFilterId));
        }
        return fieldDescriptorUser;
    }

    protected boolean allowMultiply(Properties properties) {
        return multiple.extract(properties);
    }

    @Override
    protected FieldValueUser deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        TwinEntity twinEntity = twinField.getTwin();
        twinService.loadTwinFields(twinEntity);
        List<TwinFieldUserEntity> twinFieldUserEntityList = twinEntity.getTwinFieldUserKit().getGrouped(twinField.getTwinClassField().getId());
        FieldValueUser ret = new FieldValueUser(twinField.getTwinClassField());
        if (twinFieldUserEntityList != null)
            for (TwinFieldUserEntity twinFieldDataListEntity : twinFieldUserEntityList) {
                ret.add(twinFieldDataListEntity.getUser());
            }
        return ret;
    }

    @Override
    public int getLongListThreshold(Properties properties) {
        return longListThreshold.extract(properties);
    }
}
