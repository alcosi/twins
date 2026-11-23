package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.twin.TwinGroup;
import org.twins.core.dto.rest.twin.TwinGroupKeyDTOv1;
import org.twins.core.dto.rest.twin.TwinGroupedSearchGroupDTOv1;
import org.twins.core.enums.twin.TwinGroupKeyType;
import org.twins.core.featurer.fieldtyper.value.*;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.datalist.DataListOptionRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.*;
import org.twins.core.mappers.rest.twinclass.TwinClassRestDTOMapper;
import org.twins.core.mappers.rest.twinstatus.TwinStatusRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;

import java.util.*;

/**
 * TwinGroup tree -> DTO tree. Loads nothing by id: the entity behind every group key rides inside
 * TwinGroup.keyValue (loaded by the search / TwinGroupedSearchService), this mapper only postpones it
 * into relatedObjects under its show-mode (same style as a twin relation mapper reading twin.assignerUser).
 */
@Component
@RequiredArgsConstructor
public class TwinGroupedSearchGroupRestDTOMapper extends RestSimpleDTOMapper<TwinGroup, TwinGroupedSearchGroupDTOv1> {
    @MapperModePointerBinding(modes = {UserMode.TwinGroupKey2UserMode.class})
    private final UserRestDTOMapper userDTOMapper;

    @MapperModePointerBinding(modes = {TwinClassMode.TwinGroupKey2TwinClassMode.class})
    private final TwinClassRestDTOMapper twinClassRestDTOMapper;

    @MapperModePointerBinding(modes = {StatusMode.TwinGroupKey2StatusMode.class})
    private final TwinStatusRestDTOMapper twinStatusRestDTOMapper;

    @MapperModePointerBinding(modes = {TwinMode.class, TwinMode.TwinGroupKey2TwinMode.class})
    private final TwinBaseRestDTOMapper twinBaseRestDTOMapper;

    @MapperModePointerBinding(modes = {DataListOptionMode.TwinGroupKey2DataListOptionMode.class})
    private final DataListOptionRestDTOMapper dataListOptionRestDTOMapper;

    private final TwinRestDTOMapperV2 twinRestDTOMapperV2;

    @Override
    public void map(TwinGroup src, TwinGroupedSearchGroupDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst.setKey(key(src));
        dst.setCount((long) src.getCount());
        postponeKeyValue(src, mapperContext);
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
     * Bulk preload for a tree conversion: relations of the leaf twins and of the key twins
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

    /**
     * Postpones the entity behind the key (if any) into relatedObjects under its show-mode. Scalar keys
     * (text, boolean, date) and the empty key carry no entity and are skipped.
     */
    private void postponeKeyValue(TwinGroup src, MapperContext mapperContext) throws Exception {
        if (mapperContext.isLazyRelations()) {
            return;
        }
        FieldValueSingle keyValue = src.getKeyValue();
        if (keyValue instanceof FieldValueUserSingle user) {
            if (user.getValue() != null && mapperContext.hasModeButNot(UserMode.TwinGroupKey2UserMode.HIDE)) {
                userDTOMapper.postpone(user.getValue(),
                        mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.TwinGroupKey2UserMode.SHORT)));
            }
            return;
        }
        if (keyValue instanceof FieldValueStatus status) {
            if (status.getValue() != null && mapperContext.hasModeButNot(StatusMode.TwinGroupKey2StatusMode.HIDE)) {
                twinStatusRestDTOMapper.postpone(status.getValue(),
                        mapperContext.forkOnPoint(mapperContext.getModeOrUse(StatusMode.TwinGroupKey2StatusMode.SHORT)));
            }
            return;
        }
        if (keyValue instanceof FieldValueTwinClassSingle twinClass) {
            if (twinClass.getValue() != null && mapperContext.hasModeButNot(TwinClassMode.TwinGroupKey2TwinClassMode.HIDE)) {
                twinClassRestDTOMapper.postpone(twinClass.getValue(),
                        mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinClassMode.TwinGroupKey2TwinClassMode.SHORT)));
            }
            return;
        }
        if (keyValue instanceof FieldValueSelectSingle select) {
            if (select.getValue() != null && mapperContext.hasModeButNot(DataListOptionMode.TwinGroupKey2DataListOptionMode.HIDE)) {
                dataListOptionRestDTOMapper.postpone(select.getValue(),
                        mapperContext.forkOnPoint(mapperContext.getModeOrUse(DataListOptionMode.TwinGroupKey2DataListOptionMode.SHORT)));
            }
            return;
        }
        if (keyValue instanceof FieldValueLinkSingle link
                && link.getValue() != null
                && mapperContext.hasModeButNot(TwinMode.TwinGroupKey2TwinMode.HIDE)) {
            twinBaseRestDTOMapper.postpone(link.getValue(),
                    mapperContext.forkOnPoint(mapperContext.getModeOrUse(TwinMode.TwinGroupKey2TwinMode.SHORT)));
        }
    }

    private static void collect(Collection<TwinGroup> nodes, List<TwinEntity> twins, List<TwinEntity> keyTwins) {
        if (nodes == null) {
            return;
        }
        for (TwinGroup node : nodes) {
            if (node.getTwins() != null) {
                twins.addAll(node.getTwins());
            }
            if (node.getKeyValue() instanceof FieldValueLinkSingle link && link.getValue() != null) {
                keyTwins.add(link.getValue());
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
                .setType(keyTypeOf(group.getKeyValue()))
                .setValue(group.getKey());
    }

    private static TwinGroupKeyType keyTypeOf(FieldValueSingle keyValue) {
        if (keyValue instanceof FieldValueUserSingle) {
            return TwinGroupKeyType.USER;
        }
        if (keyValue instanceof FieldValueStatus) {
            return TwinGroupKeyType.STATUS;
        }
        if (keyValue instanceof FieldValueTwinClassSingle) {
            return TwinGroupKeyType.TWIN_CLASS;
        }
        if (keyValue instanceof FieldValueLinkSingle) {
            return TwinGroupKeyType.TWIN;
        }
        if (keyValue instanceof FieldValueSelectSingle) {
            return TwinGroupKeyType.DATA_LIST_OPTION;
        }
        if (keyValue instanceof FieldValueBoolean) {
            return TwinGroupKeyType.BOOLEAN;
        }
        if (keyValue instanceof FieldValueDate) {
            return TwinGroupKeyType.TIMESTAMP;
        }
        if (keyValue instanceof FieldValueSimple) {
            // FieldValueText; a decimal field value is also its formatted text
            return TwinGroupKeyType.TEXT;
        }
        throw new IllegalStateException("Unsupported group key value " + keyValue);
    }
}
