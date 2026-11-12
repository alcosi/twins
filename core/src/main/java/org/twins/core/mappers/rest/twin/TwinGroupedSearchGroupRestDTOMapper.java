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
import org.twins.core.domain.twin.TwinGroupKeyKind;
import org.twins.core.dto.rest.twin.TwinDTOv2;
import org.twins.core.dto.rest.twin.TwinGroupKeyDTOv1;
import org.twins.core.dto.rest.twin.TwinGroupedSearchGroupDTOv1;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @MapperModePointerBinding(modes = {DataListOptionMode.class})
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
        List<TwinDTOv2> leaves = new ArrayList<>(twins.size());
        for (TwinEntity twin : twins) {
            leaves.add(twinRestDTOMapperV2.convert(twin, mapperContext));
        }
        dst.setTwins(leaves);
    }

    @Override
    public void beforeCollectionConversion(Collection<TwinGroup> srcCollection, MapperContext mapperContext) throws Exception {
        if (srcCollection == null || srcCollection.isEmpty()) {
            return;
        }
        List<TwinGroup> nodes = new ArrayList<>();
        List<TwinEntity> twins = new ArrayList<>();
        collect(srcCollection, nodes, twins);
        twinRestDTOMapperV2.beforeCollectionConversion(twins, mapperContext);
        loadUsers(ids(nodes, TwinGroupKeyKind.ASSIGNER_USER, TwinGroupKeyKind.OWNER_USER, TwinGroupKeyKind.CREATOR_USER), mapperContext);
        loadStatuses(ids(nodes, TwinGroupKeyKind.STATUS), mapperContext);
        loadClasses(ids(nodes, TwinGroupKeyKind.TWIN_CLASS), mapperContext);
        loadHeads(ids(nodes, TwinGroupKeyKind.HEAD), mapperContext);
        loadLinkTwins(ids(nodes, TwinGroupKeyKind.LINK), mapperContext);
        loadOptions(ids(nodes, TwinGroupKeyKind.FLAVOR), mapperContext, DataListOptionMode.TwinFlavor2DataListOptionMode.SHORT);
        loadOptions(dataListIds(nodes), mapperContext, DataListOptionMode.TwinField2DataListOptionMode.SHORT);
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
        twinStatusRestDTOMapper.postpone(statuses.getCollection(), mapperContext.forkOnPoint(StatusMode.Twin2StatusMode.SHORT));
    }

    private void loadClasses(List<UUID> ids, MapperContext mapperContext) throws Exception {
        if (ids.isEmpty() || !mapperContext.hasModeButNot(TwinClassMode.Twin2TwinClassMode.HIDE)) {
            return;
        }
        Kit<TwinClassEntity, UUID> classes = twinClassService.findEntities(ids,
                EntitySmartService.ListFindMode.ifMissedIgnore,
                EntitySmartService.ReadPermissionCheckMode.none,
                EntitySmartService.EntityValidateMode.none);
        twinClassRestDTOMapper.postpone(classes.getCollection(), mapperContext.forkOnPoint(TwinClassMode.Twin2TwinClassMode.SHORT));
    }

    private void loadHeads(List<UUID> ids, MapperContext mapperContext) throws Exception {
        if (ids.isEmpty() || !mapperContext.hasModeButNot(TwinMode.HIDE)) {
            return;
        }
        Kit<TwinEntity, UUID> heads = twinService.findEntities(ids,
                EntitySmartService.ListFindMode.ifMissedIgnore,
                EntitySmartService.ReadPermissionCheckMode.none,
                EntitySmartService.EntityValidateMode.none);
        twinBaseRestDTOMapper.postpone(heads.getCollection(), mapperContext);
    }

    private void loadLinkTwins(List<UUID> ids, MapperContext mapperContext) throws Exception {
        if (ids.isEmpty() || !mapperContext.hasModeButNot(TwinMode.HIDE)) {
            return;
        }
        Kit<TwinEntity, UUID> nodes = twinService.findEntities(ids,
                EntitySmartService.ListFindMode.ifMissedIgnore,
                EntitySmartService.ReadPermissionCheckMode.ifDeniedLog,
                EntitySmartService.EntityValidateMode.none);
        twinBaseRestDTOMapper.postpone(nodes.getCollection(), mapperContext);
    }

    private void loadOptions(List<UUID> ids, MapperContext mapperContext, MapperModePointer<DataListOptionMode> mode) throws Exception {
        if (ids.isEmpty() || !mapperContext.hasModeButNot(DataListOptionMode.HIDE)) {
            return;
        }
        Kit<DataListOptionEntity, UUID> options = dataListOptionService.findEntities(ids,
                EntitySmartService.ListFindMode.ifMissedIgnore,
                EntitySmartService.ReadPermissionCheckMode.none,
                EntitySmartService.EntityValidateMode.none);
        dataListOptionRestDTOMapper.postpone(options.getCollection(), mapperContext.forkOnPoint(mode));
    }

    private static TwinGroupKeyDTOv1 key(TwinGroup group) {
        UUID id = group.getKey() instanceof UUID uuid ? uuid : null;
        return switch (group.getKeyKind()) {
            case ASSIGNER_USER -> new TwinGroupKeyDTOv1.Assigner().setAssignerUserId(id);
            case OWNER_USER -> new TwinGroupKeyDTOv1.Owner().setOwnerUserId(id);
            case CREATOR_USER -> new TwinGroupKeyDTOv1.Creator().setCreatedByUserId(id);
            case STATUS -> new TwinGroupKeyDTOv1.Status().setTwinStatusId(id);
            case TWIN_CLASS -> new TwinGroupKeyDTOv1.TwinClass().setTwinClassId(id);
            case HEAD -> new TwinGroupKeyDTOv1.Head().setHeadTwinId(id);
            case FLAVOR -> new TwinGroupKeyDTOv1.Flavor().setFlavorDataListOptionId(id);
            case DYNAMIC -> new TwinGroupKeyDTOv1.Dynamic().setGroupedFieldValues(dynamicValues(group));
            case LINK -> new TwinGroupKeyDTOv1.Link().setLinkId(group.getLinkId()).setLinkTwinId(id);
        };
    }

    private static Map<UUID, Object> dynamicValues(TwinGroup group) {
        Map<UUID, Object> values = new HashMap<>();
        values.put(group.getGroupFieldId(), group.getKey());
        return values;
    }

    private static List<UUID> ids(Collection<TwinGroup> groups, TwinGroupKeyKind... kinds) {
        Set<TwinGroupKeyKind> accepted = Set.of(kinds);
        List<UUID> ids = new ArrayList<>();
        for (TwinGroup group : groups) {
            if (accepted.contains(group.getKeyKind()) && group.getKey() instanceof UUID id) {
                ids.add(id);
            }
        }
        return ids;
    }

    private static List<UUID> dataListIds(Collection<TwinGroup> groups) {
        List<UUID> ids = new ArrayList<>();
        for (TwinGroup group : groups) {
            if (group.getKeyKind() == TwinGroupKeyKind.DYNAMIC && group.isDataListOption() && group.getKey() instanceof UUID id) {
                ids.add(id);
            }
        }
        return ids;
    }
}
