package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
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
import org.twins.core.mappers.rest.mappercontext.modes.*;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.twinstatus.TwinStatusRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;

import java.util.*;

/**
 * TwinGroup tree -> DTO tree. Loads nothing by id: the entities behind group keys are attached to
 * TwinGroup.keyEntity by TwinGroupedSearchService.loadKeyEntities, this mapper only postpones them into
 * relatedObjects under their show-modes (same style as a twin relation mapper reading twin.assignerUser).
 */
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
     * Bulk preload for a tree conversion: relations of the leaf twins and of the TWIN key entities
     * (heads / link ends). Both were loaded by the search already; this only fills their relation kits
     * in bulk so the postponed conversion stays N+1-free.
     */
    @Override
    public void beforeCollectionConversion(Collection<TwinGroup> srcCollection, MapperContext mapperContext) throws Exception {
        if (srcCollection == null || srcCollection.isEmpty() || mapperContext.isLazyRelations()) {
            return;
        }
        List<TwinEntity> twins = new ArrayList<>();
        List<TwinEntity> keyTwins = new ArrayList<>();
        collect(srcCollection, twins, keyTwins);
        if (!mapperContext.hasModeButNot(TwinMode.HIDE)) {
            return;
        }
        if (!twins.isEmpty()) {
            twinRestDTOMapperV2.beforeCollectionConversion(uniqueTwins(twins), mapperContext);
        }
        if (!keyTwins.isEmpty()) {
            twinBaseRestDTOMapper.beforeCollectionConversion(uniqueTwins(keyTwins), mapperContext);
        }
    }

    private void postponeKeyEntity(TwinGroup src, MapperContext mapperContext) throws Exception {
        if (mapperContext.isLazyRelations() || src.getKeyType() == null || src.getKeyEntity() == null) {
            return;
        }
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

    private static void collect(Collection<TwinGroup> nodes, List<TwinEntity> twins, List<TwinEntity> keyTwins) {
        if (nodes == null) {
            return;
        }
        for (TwinGroup node : nodes) {
            if (node.getTwins() != null) {
                twins.addAll(node.getTwins());
            }
            if (node.getKeyType() == TwinGroupKeyType.TWIN && node.getKeyEntity() instanceof TwinEntity keyTwin) {
                keyTwins.add(keyTwin);
            }
            collect(node.getGroups(), twins, keyTwins);
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
