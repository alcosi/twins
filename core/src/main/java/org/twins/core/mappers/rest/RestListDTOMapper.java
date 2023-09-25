package org.twins.core.mappers.rest;


import java.util.ArrayList;
import java.util.List;

public abstract class RestListDTOMapper<T, S> implements RestDTOMapper<T, S> {
    public List<S> convertList(List<T> srcList, MapperProperties mapperProperties) throws Exception {
        List<S> ret = new ArrayList<>();
        for (T src : srcList) {
            ret.add(this.convert(src, mapperProperties));
        }
        return ret;
    }

    public List<S> convertList(List<T> srcList) throws Exception {
        if (srcList == null)
            return null;
        return convertList(srcList, new MapperProperties());
    }
}
