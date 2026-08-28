package org.twins.core.featurer.fieldtyper;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.twins.core.dao.history.context.HistoryContextUserMultiChange;
import org.twins.core.dao.specifications.twin.TwinSpecification;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldUserEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldValueSearchUser;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorUser;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageUser;
import org.twins.core.featurer.fieldtyper.value.FieldValueUser;
import org.twins.core.service.history.HistoryItem;
import org.twins.core.service.user.UserFilterService;
import org.twins.core.service.user.UserService;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Append-only counterpart of {@link FieldTyperUser}.
 * <p>
 * Unlike {@code FieldTyperUser}, this typer has no {@code multiple} parameter (it is always a
 * multi-select) and serialization never replaces the stored set — instead the provided users are
 * appended to the already stored ones, with duplicates skipped. No stored users are removed on
 * serialization. Removing users must go through the clear/nullify path, not through this value.
 */
@Slf4j
@Component
@Lazy
@Featurer(id = FeaturerTwins.ID_1358,
        name = "User Increment",
        description = "User field that appends the provided users to the already stored ones (duplicates are skipped, nothing is removed on serialization)")
public class FieldTyperUserIncrement extends FieldTyper<FieldDescriptorUser, FieldValueUser, TwinFieldStorageUser, TwinFieldValueSearchUser> implements LongList {
    @Autowired
    @Lazy
    UserFilterService userFilterService;
    @Autowired
    UserService userService;

    @FeaturerParam(name = "User filter UUID", description = "", order = 1)
    public static final FeaturerParamUUID userFilterUUID = new FeaturerParamUUID("userFilterUUID"); //todo change type

    @FeaturerParam(name = "Long list threshold", description = "If options count is bigger then given threshold longList type will be used", order = 2)
    public static final FeaturerParamInt longListThreshold = new FeaturerParamInt("longListThreshold");

    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, FieldValueUser value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        List<UserEntity> selectedUserEntityList = userService.findEntitiesSafe(value.getItems().stream().map(UserEntity::getId).toList()).getList();
        twinService.loadTwinFields(twin);
        Map<UUID, TwinFieldUserEntity> storedFieldUsers = null;
        if (twin.getTwinFieldUserKit().containsGroupedKey(value.getTwinClassField().getId()))
            storedFieldUsers = twin.getTwinFieldUserKit().getGrouped(value.getTwinClassField().getId()).stream().collect(Collectors.toMap(TwinFieldUserEntity::getUserId, Function.identity()));

        HistoryItem<HistoryContextUserMultiChange> historyItem = historyService.fieldChangeUserMulti(value.getTwinClassField());
        for (UserEntity userEntity : selectedUserEntityList) {
            //todo check if user valid for current filter result
            // append-only: skip already stored users to avoid duplicates, never delete the rest
            if (FieldValueChangeHelper.notSaved(userEntity.getId(), storedFieldUsers)) {
                if (twinChangesCollector.isHistoryCollectorEnabled())
                    historyItem.getContext().shotAddedUserId(userEntity.getId());
                twinChangesCollector.add(TwinFieldUserEntity.of(twin, value.getTwinClassField())
                        .setUserId(checkUserAllowed(twin, value.getTwinClassField(), userEntity))
                        .setUser(userEntity));
            }
        }
        if (twinChangesCollector.isHistoryCollectorEnabled() && historyItem.getContext().notEmpty())
            twinChangesCollector.getHistoryCollector(twin).add(historyItem);
    }

    public UUID checkUserAllowed(TwinEntity twinEntity, TwinClassFieldEntity twinClassFieldEntity, UserEntity userEntity) throws ServiceException {
        return userEntity.getId(); // can be overridden in case if value must be shared between twins
    }

    @Override
    public FieldDescriptorUser getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException {
        UUID userFilterId = userFilterUUID.extract(properties);
        int listSize = userFilterService.countFilterResult(userFilterId);
        FieldDescriptorUser fieldDescriptorUser = new FieldDescriptorUser()
                .multiple(true); // increment is always a multi-select: it appends to a set of users
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
        List<TwinFieldUserEntity> twinFieldUserEntityList = twinEntity.getTwinFieldUserKit().getGrouped(twinField.getTwinClassField().getId());
        FieldValueUser ret = new FieldValueUser(twinField.getTwinClassField());
        if (twinFieldUserEntityList != null) {
            for (TwinFieldUserEntity twinFieldDataListEntity : twinFieldUserEntityList) {
                ret.add(twinFieldDataListEntity.getUser());
            }
        }
        return ret;
    }

    @Override
    public int getLongListThreshold(Properties properties) {
        return longListThreshold.extract(properties);
    }

    @Override
    public Specification<TwinEntity> searchBy(TwinFieldValueSearchUser search) throws ServiceException {
        return TwinSpecification.checkFieldUser(search);
    }
}
