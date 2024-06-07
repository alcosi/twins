package org.twins.core.mappers.rest;


import java.util.*;

public abstract class RestListDTOMapper<T, S> implements RestDTOMapper<T, S> {
    public List<S> convertList(Collection<T> srcCollection, MapperContext mapperContext) throws Exception {
        if (srcCollection == null)
            return null;
        beforeListConversion(srcCollection, mapperContext);
        List<S> ret = new ArrayList<>();
        for (T src : srcCollection) {
            S converted = this.convert(src, mapperContext);
            if (converted != null)
                ret.add(converted);
        }
        return ret;
    }

    public List<S> convertList(Collection<T> srcList) throws Exception {
        if (srcList == null)
            return null;
        return convertList(srcList, new MapperContext());
    }

    public Map<UUID, S> convertMap(Map<UUID, T> srcMap, MapperContext mapperContext) throws Exception {
        if (srcMap == null)
            return null;
        beforeListConversion(srcMap.values(), mapperContext);
        Map<UUID, S> ret = new LinkedHashMap<>();
        for (Map.Entry<UUID, T> src : srcMap.entrySet()) {
            ret.put(src.getKey(), this.convert(src.getValue(), mapperContext));
        }
        return ret;
    }

    public void beforeListConversion(Collection<T> srcCollection, MapperContext mapperContext) throws Exception {

    }
}
