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
import org.twins.core.mappers.rest.mappercontext.MapperModePointer;
import org.twins.core.mappers.rest.mappercontext.modes.DataListOptionMode;
import org.twins.core.mappers.rest.mappercontext.modes.StatusMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
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
        postponeKeyEntity(src, mapperContext);
        if (src.getGroups() != null) {
            List<TwinGroupedSearchGroupDTOv1> nested = new ArrayList<>(src.getGroups().size());
            for (TwinGroup child : src.getGroups()) {
                nested.add(convert(child, mapperContext));
            }
            dst.setGroups(nested);
            return;
        }
        List<TwinEntity> twins = src.getTwins() == null ? List.of() : src.getTwins();
        if (!mapperContext.isLazyRelations() && mapperContext.hasModeButNot(TwinMode.HIDE)) {
            twinRestDTOMapperV2.postpone(twins, mapperContext);
        }
        List<UUID> ids = new ArrayList<>(twins.size());
        for (TwinEntity twin : twins) {
            if (twin.getId() != null) {
                ids.add(twin.getId());
            }
        }
        dst.setTwinIdList(ids);
    }

    /**
     * Batch loading only. All postpone calls live in map() so that a single-object convert()
     * (which does not invoke beforeCollectionConversion) still fills relatedObjects.
     * Skips work when lazyRelation=true: postpone is a no-op and relatedObjects stay empty.
     */
    @Override
    public void beforeCollectionConversion(Collection<TwinGroup> srcCollection, MapperContext mapperContext) throws Exception {
        if (srcCollection == null || srcCollection.isEmpty() || mapperContext.isLazyRelations()) {
            return;
        }
        List<TwinGroup> nodes = new ArrayList<>();
        List<TwinEntity> twins = new ArrayList<>();
        collect(srcCollection, nodes, twins);
        if (mapperContext.hasModeButNot(TwinMode.HIDE)) {
            twinRestDTOMapperV2.beforeCollectionConversion(uniqueTwins(twins), mapperContext);
        }
        loadKeyEntities(nodes, mapperContext);
    }

    private void postponeKeyEntity(TwinGroup src, MapperContext mapperContext) throws Exception {
        if (src.getKeyType() == null || mapperContext.isLazyRelations()) {
            return;
        }
        ensureKeyEntity(src, mapperContext);
        switch (src.getKeyType()) {
            case USER -> {
                if (mapperContext.hasModeButNot(UserMode.Twin2UserMode.HIDE)) {
                    userDTOMapper.postpone((UserEntity) src.getKeyEntity(),
                            mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.Twin2UserMode.SHORT)));
                }
            }
            case STATUS -> {
                if (mapperContext.hasModeButNot(StatusMode.Twin2StatusMode.HIDE)) {
                    twinStatusRestDTOMapper.postpone((TwinStatusEntity) src.getKeyEntity(),
                            mapperContext.forkOnPoint(mapperContext.getModeOrUse(StatusMode.Twin2StatusMode.SHORT)));
                }
            }
            case TWIN_CLASS -> {
                if (mapperContext.hasModeButNot(TwinClassMode.Twin2TwinClassMode.HIDE)) {
                    twinClassRestDTOMapper.postpone((TwinClassEntity) src.getKeyEntity(),
                            mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinClassMode.Twin2TwinClassMode.SHORT)));
                }
            }
            case TWIN -> {
                if (mapperContext.hasModeButNot(TwinMode.HIDE)) {
                    twinBaseRestDTOMapper.postpone((TwinEntity) src.getKeyEntity(), mapperContext);
                }
            }
            case DATA_LIST_OPTION_FLAVOR -> postponeDataListOption(src, mapperContext,
                    DataListOptionMode.TwinFlavor2DataListOptionMode.HIDE,
                    DataListOptionMode.TwinFlavor2DataListOptionMode.SHORT);
            case DATA_LIST_OPTION_FIELD -> postponeDataListOption(src, mapperContext,
                    DataListOptionMode.TwinField2DataListOptionMode.HIDE,
                    DataListOptionMode.TwinField2DataListOptionMode.SHORT);
            default -> {
                // TEXT, DECIMAL, BOOLEAN, TIMESTAMP keys have no entity behind them
            }
        }
    }

    private <M extends MapperModePointer<DataListOptionMode>> void postponeDataListOption(
            TwinGroup src, MapperContext mapperContext, M hidden, M fallback) {
        if (!mapperContext.hasModeButNot(hidden)) {
            return;
        }
        dataListOptionRestDTOMapper.postpone((DataListOptionEntity) src.getKeyEntity(),
                mapperContext.forkOnPoint(mapperContext.getModeOrUse(fallback)));
    }

    private void ensureKeyEntity(TwinGroup src, MapperContext mapperContext) throws Exception {
        if (src.isKeyEntityResolved()) {
            return;
        }
        loadKeyEntity(src, mapperContext);
    }

    private void loadKeyEntities(Collection<TwinGroup> nodes, MapperContext mapperContext) throws Exception {
        loadUsers(nodes, mapperContext);
        loadStatuses(nodes, mapperContext);
        loadClasses(nodes, mapperContext);
        loadTwins(nodes, mapperContext);
        loadOptions(nodes, TwinGroupKeyType.DATA_LIST_OPTION_FLAVOR, mapperContext,
                DataListOptionMode.TwinFlavor2DataListOptionMode.HIDE);
        loadOptions(nodes, TwinGroupKeyType.DATA_LIST_OPTION_FIELD, mapperContext,
                DataListOptionMode.TwinField2DataListOptionMode.HIDE);
    }

    private void loadKeyEntity(TwinGroup node, MapperContext mapperContext) throws Exception {
        List<TwinGroup> nodes = List.of(node);
        switch (node.getKeyType()) {
            case USER -> loadUsers(nodes, mapperContext);
            case STATUS -> loadStatuses(nodes, mapperContext);
            case TWIN_CLASS -> loadClasses(nodes, mapperContext);
            case TWIN -> loadTwins(nodes, mapperContext);
            case DATA_LIST_OPTION_FLAVOR -> loadOptions(nodes, TwinGroupKeyType.DATA_LIST_OPTION_FLAVOR, mapperContext,
                    DataListOptionMode.TwinFlavor2DataListOptionMode.HIDE);
            case DATA_LIST_OPTION_FIELD -> loadOptions(nodes, TwinGroupKeyType.DATA_LIST_OPTION_FIELD, mapperContext,
                    DataListOptionMode.TwinField2DataListOptionMode.HIDE);
            default -> {
            }
        }
    }

    private void loadUsers(Collection<TwinGroup> nodes, MapperContext mapperContext) throws Exception {
        load(nodes, TwinGroupKeyType.USER, mapperContext.hasModeButNot(UserMode.Twin2UserMode.HIDE), ids ->
                userService.findEntities(ids,
                        EntitySmartService.ListFindMode.ifMissedIgnore,
                        EntitySmartService.ReadPermissionCheckMode.none,
                        EntitySmartService.EntityValidateMode.none), null);
    }

    private void loadStatuses(Collection<TwinGroup> nodes, MapperContext mapperContext) throws Exception {
        load(nodes, TwinGroupKeyType.STATUS, mapperContext.hasModeButNot(StatusMode.Twin2StatusMode.HIDE), ids ->
                twinStatusService.findEntities(ids,
                        EntitySmartService.ListFindMode.ifMissedIgnore,
                        EntitySmartService.ReadPermissionCheckMode.none,
                        EntitySmartService.EntityValidateMode.none), null);
    }

    private void loadClasses(Collection<TwinGroup> nodes, MapperContext mapperContext) throws Exception {
        load(nodes, TwinGroupKeyType.TWIN_CLASS, mapperContext.hasModeButNot(TwinClassMode.Twin2TwinClassMode.HIDE), ids ->
                twinClassService.findEntities(ids,
                        EntitySmartService.ListFindMode.ifMissedIgnore,
                        EntitySmartService.ReadPermissionCheckMode.none,
                        EntitySmartService.EntityValidateMode.none), null);
    }

    private void loadTwins(Collection<TwinGroup> nodes, MapperContext mapperContext) throws Exception {
        load(nodes, TwinGroupKeyType.TWIN, mapperContext.hasModeButNot(TwinMode.HIDE), ids ->
                twinService.findEntities(List.copyOf(new LinkedHashSet<>(ids)),
                        EntitySmartService.ListFindMode.ifMissedIgnore,
                        EntitySmartService.ReadPermissionCheckMode.ifDeniedLog,
                        EntitySmartService.EntityValidateMode.none), collection ->
                twinBaseRestDTOMapper.beforeCollectionConversion(collection, mapperContext));
    }

    private void loadOptions(Collection<TwinGroup> nodes, TwinGroupKeyType type, MapperContext mapperContext,
                             MapperModePointer<DataListOptionMode> hidden) throws Exception {
        load(nodes, type, mapperContext.hasModeButNot(hidden), ids ->
                dataListOptionService.findEntities(ids,
                        EntitySmartService.ListFindMode.ifMissedIgnore,
                        EntitySmartService.ReadPermissionCheckMode.none,
                        EntitySmartService.EntityValidateMode.none), null);
    }

    private <E> void load(Collection<TwinGroup> nodes, TwinGroupKeyType type, boolean enabled,
                          KeyFinder<E> finder, AfterFind<E> afterFind) throws Exception {
        if (!enabled) {
            return;
        }
        List<UUID> ids = missedKeyIds(nodes, type);
        if (ids.isEmpty()) {
            return;
        }
        Kit<E, UUID> kit = finder.find(ids);
        if (afterFind != null) {
            afterFind.accept(kit.getCollection());
        }
        setKeyEntities(nodes, type, kit.getMap());
    }

    @FunctionalInterface
    private interface KeyFinder<E> {
        Kit<E, UUID> find(List<UUID> ids) throws Exception;
    }

    @FunctionalInterface
    private interface AfterFind<E> {
        void accept(Collection<E> entities) throws Exception;
    }

    private static List<UUID> missedKeyIds(Collection<TwinGroup> nodes, TwinGroupKeyType type) {
        List<UUID> ids = new ArrayList<>();
        for (TwinGroup node : nodes) {
            if (node.getKeyType() == type && !node.isKeyEntityResolved() && node.getKey() instanceof UUID id) {
                ids.add(id);
            }
        }
        return ids;
    }

    private static void setKeyEntities(Collection<TwinGroup> nodes, TwinGroupKeyType type, Map<UUID, ?> entities) {
        for (TwinGroup node : nodes) {
            if (node.getKeyType() == type && !node.isKeyEntityResolved() && node.getKey() instanceof UUID id) {
                node.setKeyEntity(entities.get(id));
                node.setKeyEntityResolved(true);
            }
        }
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

    private static List<TwinEntity> uniqueTwins(List<TwinEntity> twins) {
        List<TwinEntity> unique = new ArrayList<>();
        Set<UUID> seen = new LinkedHashSet<>();
        for (TwinEntity twin : twins) {
            if (twin.getId() != null && seen.add(twin.getId())) {
                unique.add(twin);
            }
        }
        return unique;
    }

    private static TwinGroupKeyDTOv1 key(TwinGroup group) {
        return new TwinGroupKeyDTOv1()
                .setType(group.getKeyType())
                .setValue(group.getKey());
    }
}
