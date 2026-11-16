package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.cambium.common.kit.Kit;
import org.cambium.service.EntitySmartService;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.twin.TwinGroup;
import org.twins.core.dto.rest.twin.TwinGroupKeyDTOv1;
import org.twins.core.dto.rest.twin.TwinGroupedSearchGroupDTOv1;
import org.twins.core.enums.twin.TwinGroupKeyType;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.datalist.DataListOptionRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.DataListOptionMode;
import org.twins.core.mappers.rest.mappercontext.modes.StatusMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.mappercontext.MapperModePointer;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.twinstatus.TwinStatusRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.datalist.DataListOptionService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinclass.TwinClassService;
import org.twins.core.service.twinstatus.TwinStatusService;
import org.twins.core.service.user.UserService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TwinGroupedSearchGroupRestDTOMapper extends RestSimpleDTOMapper<TwinGroup, TwinGroupedSearchGroupDTOv1> {
    @MapperModePointerBinding(modes = {UserMode.Twin2UserMode.class})
    private final UserRestDTOMapper userDTOMapper;

    @MapperModePointerBinding(modes = {TwinClassMode.Twin2TwinClassMode.class})
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;

    @MapperModePointerBinding(modes = {StatusMode.Twin2StatusMode.class})
    private final TwinStatusRestDTOMapper twinStatusRestDTOMapper;

    @MapperModePointerBinding(modes = {TwinMode.class})
    private final TwinBaseRestDTOMapper twinBaseRestDTOMapper;

    @MapperModePointerBinding(modes = {
            DataListOptionMode.TwinFlavor2DataListOptionMode.class,
            DataListOptionMode.TwinField2DataListOptionMode.class
    })
    private final DataListOptionRestDTOMapper dataListOptionRestDTOMapper;

    private final TwinRestDTOMapperV2 twinRestDTOMapperV2;
    private final UserService userService;
    private final TwinStatusService twinStatusService;
    private final TwinClassService twinClassService;
    private final TwinService twinService;
    private final DataListOptionService dataListOptionService;

    @Override
    public void map(TwinGroup src, TwinGroupedSearchGroupDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst.setKey(key(src));
        dst.setCount((long) src.getCount());
        if (src.getGroups() != null) {
            List<TwinGroupedSearchGroupDTOv1> nested = new ArrayList<>(src.getGroups().size());
            for (TwinGroup child : src.getGroups()) {
                nested.add(convert(child, mapperContext));
            }
            dst.setGroups(nested);
            return;
        }
        List<TwinEntity> twins = src.getTwins() == null ? List.of() : src.getTwins();
        List<UUID> ids = new ArrayList<>(twins.size());
        for (TwinEntity twin : twins) {
            if (twin.getId() != null) {
                ids.add(twin.getId());
            }
        }
        dst.setTwinIdList(ids);
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinGroup> srcCollection, MapperContext mapperContext) throws Exception {
        if (srcCollection == null || srcCollection.isEmpty()) {
            return;
        }
        List<TwinGroup> nodes = new ArrayList<>();
        List<TwinEntity> twins = new ArrayList<>();
        collect(srcCollection, nodes, twins);
        if (mapperContext.hasModeButNot(TwinMode.HIDE)) {
            List<TwinEntity> unique = new ArrayList<>();
            Set<UUID> seen = new java.util.LinkedHashSet<>();
            for (TwinEntity twin : twins) {
                if (twin.getId() != null && seen.add(twin.getId())) {
                    unique.add(twin);
                }
            }
            twinRestDTOMapperV2.beforeCollectionConversion(unique, mapperContext);
            twinRestDTOMapperV2.postpone(unique, mapperContext);
        }
        loadUsers(ids(nodes, TwinGroupKeyType.USER), mapperContext);
        loadStatuses(ids(nodes, TwinGroupKeyType.STATUS), mapperContext);
        loadClasses(ids(nodes, TwinGroupKeyType.TWIN_CLASS), mapperContext);
        loadTwins(ids(nodes, TwinGroupKeyType.TWIN), mapperContext);
        loadOptions(ids(nodes, TwinGroupKeyType.DATA_LIST_OPTION_FLAVOR), mapperContext,
                DataListOptionMode.TwinFlavor2DataListOptionMode.HIDE,
                DataListOptionMode.TwinFlavor2DataListOptionMode.SHORT);
        loadOptions(ids(nodes, TwinGroupKeyType.DATA_LIST_OPTION_FIELD), mapperContext,
                DataListOptionMode.TwinField2DataListOptionMode.HIDE,
                DataListOptionMode.TwinField2DataListOptionMode.SHORT);
    }

    private static void collect(Collection<TwinGroup> nodes, List<TwinGroup> all, List<TwinEntity> twins) {
        if (nodes == null) {
            return;
        }
        for (TwinGroup node : nodes) {
            all.add(node);
            if (node.getTwins() != null) {
                twins.addAll(node.getTwins());
            }
            collect(node.getGroups(), all, twins);
        }
    }

    private void loadUsers(List<UUID> ids, MapperContext mapperContext) throws Exception {
        if (ids.isEmpty() || !mapperContext.hasModeButNot(UserMode.Twin2UserMode.HIDE)) {
            return;
        }
        Kit<UserEntity, UUID> users = userService.findEntities(ids,
                EntitySmartService.ListFindMode.ifMissedIgnore,
                EntitySmartService.ReadPermissionCheckMode.none,
                EntitySmartService.EntityValidateMode.none);
        userDTOMapper.postpone(users.getCollection(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.Twin2UserMode.SHORT)));
    }

    private void loadStatuses(List<UUID> ids, MapperContext mapperContext) throws Exception {
        if (ids.isEmpty() || !mapperContext.hasModeButNot(StatusMode.Twin2StatusMode.HIDE)) {
            return;
        }
        Kit<TwinStatusEntity, UUID> statuses = twinStatusService.findEntities(ids,
                EntitySmartService.ListFindMode.ifMissedIgnore,
                EntitySmartService.ReadPermissionCheckMode.none,
                EntitySmartService.EntityValidateMode.none);
        twinStatusRestDTOMapper.postpone(statuses.getCollection(),
                mapperContext.forkOnPoint(mapperContext.getModeOrUse(StatusMode.Twin2StatusMode.SHORT)));
    }

    private void loadClasses(List<UUID> ids, MapperContext mapperContext) throws Exception {
        if (ids.isEmpty() || !mapperContext.hasModeButNot(TwinClassMode.Twin2TwinClassMode.HIDE)) {
            return;
        }
        Kit<TwinClassEntity, UUID> classes = twinClassService.findEntities(ids,
                EntitySmartService.ListFindMode.ifMissedIgnore,
                EntitySmartService.ReadPermissionCheckMode.none,
                EntitySmartService.EntityValidateMode.none);
        twinClassRestDTOMapper.postpone(classes.getCollection(),
                mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinClassMode.Twin2TwinClassMode.SHORT)));
    }

    private void loadTwins(List<UUID> ids, MapperContext mapperContext) throws Exception {
        if (!mapperContext.hasModeButNot(TwinMode.HIDE)) {
            return;
        }
        LinkedHashSet<UUID> unique = new LinkedHashSet<>(ids);
        unique.remove(null);
        if (unique.isEmpty()) {
            return;
        }
        Kit<TwinEntity, UUID> twins = twinService.findEntities(new ArrayList<>(unique),
                EntitySmartService.ListFindMode.ifMissedIgnore,
                EntitySmartService.ReadPermissionCheckMode.ifDeniedLog,
                EntitySmartService.EntityValidateMode.none);
        Collection<TwinEntity> entities = twins.getCollection();
        twinBaseRestDTOMapper.beforeCollectionConversion(entities, mapperContext);
        twinBaseRestDTOMapper.postpone(entities, mapperContext);
    }

    private <M extends MapperModePointer<DataListOptionMode>> void loadOptions(
            List<UUID> ids, MapperContext mapperContext, M hidden, M fallback) throws Exception {
        if (ids.isEmpty() || !mapperContext.hasModeButNot(hidden)) {
            return;
        }
        Kit<DataListOptionEntity, UUID> options = dataListOptionService.findEntities(ids,
                EntitySmartService.ListFindMode.ifMissedIgnore,
                EntitySmartService.ReadPermissionCheckMode.none,
                EntitySmartService.EntityValidateMode.none);
        dataListOptionRestDTOMapper.postpone(options.getCollection(),
                mapperContext.forkOnPoint(mapperContext.getModeOrUse(fallback)));
    }

    private static TwinGroupKeyDTOv1 key(TwinGroup group) {
        TwinGroupKeyDTOv1 key = new TwinGroupKeyDTOv1()
                .setType(group.getKeyType())
                .setValue(group.getKey());
        return key;
    }

    private static List<UUID> ids(Collection<TwinGroup> groups, TwinGroupKeyType type) {
        List<UUID> ids = new ArrayList<>();
        for (TwinGroup group : groups) {
            if (group.getKeyType() == type && group.getKey() instanceof UUID id) {
                ids.add(id);
            }
        }
        return ids;
    }

}
