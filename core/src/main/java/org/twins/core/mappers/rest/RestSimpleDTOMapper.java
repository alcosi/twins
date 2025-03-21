package org.twins.core.mappers.rest;


import org.cambium.common.kit.Kit;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.KitUtils;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.BiConsumer;

public abstract class RestSimpleDTOMapper<T, S> extends RestListDTOMapper<T, S> {
    private final Class<S> type;

    public RestSimpleDTOMapper() {
        Type t = getClass().getGenericSuperclass();
        ParameterizedType pt = (ParameterizedType) t;
        Type dstType = pt.getActualTypeArguments()[1];
        if (dstType instanceof Class<?>)
            type = (Class) dstType;
        else
            type = (Class) ((ParameterizedType) dstType).getRawType();
    }

    public S convert(T src) throws Exception {
        return convert(src, new MapperContext());
    }

    public S convert(T src, MapperContext mapperContext) throws Exception {
        if (hideMode(mapperContext))
            return null;
        if (src == null)
            return null;
        String objectCacheId = getObjectCacheId(src);
        S dst = mapperContext.getFromCache(type, objectCacheId);
        if (dst != null)
            return dst;
        dst = type.getDeclaredConstructor().newInstance();
        map(src, dst, mapperContext);
        mapperContext.putToCache(type, objectCacheId, dst);
        return dst;
    }

    public S convertOrPostpone(T src, MapperContext mapperContext) throws Exception {
        if (hideMode(mapperContext))
            return null;
        if (mapperContext.isLazyRelations())
            return convert(src, mapperContext);
        if (mapperContext.addRelatedObject(src))
            return null;
        return convert(src, mapperContext);
    }

    public List<S> convertCollectionPostpone(Collection<T> srcList, MapperContext mapperContext) throws Exception {
        if (srcList == null)
            return null;
        List<S> ret = null;
        for (T src : srcList) {
            S converted = this.convertOrPostpone(src, mapperContext);
            if (converted != null)
                ret = CollectionUtils.safeAdd(ret, this.convert(src, mapperContext));
        }
        return ret;
    }

    public void postpone(T src, MapperContext mapperContext) {
        if (hideMode(mapperContext))
            return;
        if (!mapperContext.isLazyRelations())
            mapperContext.addRelatedObject(src);
    }

    public void postpone(Collection<T> srcList, MapperContext mapperContext) {
        if (srcList == null)
            return;
        for (T src : srcList) {
            postpone(src, mapperContext);
        }
    }

    public Set<UUID> postpone(Kit<T, UUID> srcKit, MapperContext mapperContext) {
        if (KitUtils.isEmpty(srcKit) || hideMode(mapperContext))
            return Collections.emptySet();
        for (T src : srcKit) {
            mapperContext.addRelatedObject(src);
        }
        return srcKit.getIdSet();
    }

    public boolean hideMode(MapperContext mapperContext) {
        return false;
    }

    public String getObjectCacheId(T src) {
        return null;
    }

    public <F, Y> void convertOrPostpone(Kit<F, UUID> kit, S dst, RestSimpleDTOMapper<F, Y> lazyModeMapper, MapperContext mapperContext, BiConsumer<S, List<Y>> lazyModeFunction, BiConsumer<S, Set<UUID>> noLazyModeFunction) throws Exception {
        if (kit != null) {
            if (mapperContext.isLazyRelations())
                lazyModeFunction.accept(dst, lazyModeMapper.convertCollection(kit.getCollection(), mapperContext));
            else {
                noLazyModeFunction.accept(dst, kit.getIdSet());
                mapperContext.addRelatedObjectCollection(kit.getCollection());
            }
        }
    }

    public <F, Y> void convertMapOrPostpone(Kit<F, UUID> kit, S dst, RestSimpleDTOMapper<F, Y> lazyModeMapper, MapperContext mapperContext, BiConsumer<S, Map<UUID, Y>> lazyModeFunction, BiConsumer<S, Set<UUID>> noLazyModeFunction) throws Exception {
        if (kit != null && kit.isNotEmpty()) {
            if (mapperContext.isLazyRelations())
                lazyModeFunction.accept(dst, lazyModeMapper.convertMap(kit.getMap(), mapperContext));
            else {
                noLazyModeFunction.accept(dst, kit.getIdSet());
                mapperContext.addRelatedObjectCollection(kit.getCollection());
            }
        }
    }
}
