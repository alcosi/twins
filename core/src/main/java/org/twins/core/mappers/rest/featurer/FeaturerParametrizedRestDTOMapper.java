package org.twins.core.mappers.rest.featurer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.featurer.FeaturerService;
import org.cambium.featurer.annotations.FeaturerParamType;
import org.cambium.featurer.params.FeaturerParam;
import org.springframework.stereotype.Component;
import org.twins.core.dto.rest.featurer.FeaturerDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.EntityRef;
import org.twins.core.mappers.rest.mappercontext.FeaturerParams;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.FeaturerMode;
import org.twins.core.mappers.rest.system.EntityRefRestDTOMapper;

import java.util.*;

/**
 * Maps a featurer id paired with its params (hstore from the source entity) to a FeaturerDTO
 * by delegating to FeaturerRestDTOMapper. Additionally, when the featurer is requested at DETAILED mode
 * (the per-site pointer is resolved into FeaturerMode by forkOnPoint at the call site), walks the params
 * by their FeaturerParam type and postpones EntityRef objects for entity-referencing param values —
 * the referenced entities are then bulk-loaded and postponed into relatedObjects by EntityRefRestDTOMapper
 * on the drain phase. Mappers that copy featurer params should postpone FeaturerParams instead of the bare
 * featurer id, so the client gets both the featurer definition and the resolved param entities.
 * The target entity of a param type is declared by @FeaturerParamType(targetEntity = ...) on the param class;
 * param types without a targetEntity are not entity-referencing and are skipped.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class FeaturerParametrizedRestDTOMapper extends RestSimpleDTOMapper<FeaturerParams, FeaturerDTOv1> {

    private final FeaturerService featurerService;
    private final FeaturerRestDTOMapper featurerRestDTOMapper;
    private final EntityRefRestDTOMapper entityRefRestDTOMapper;

    @Override
    public FeaturerDTOv1 convert(FeaturerParams src, MapperContext mapperContext) throws Exception {
        if (hideMode(mapperContext) || src == null)
            return null;
        if (mapperContext.hasMode(FeaturerMode.DETAILED))
            postponeEntityRefs(src, mapperContext);
        if (src.getFeaturerId() == null)
            return null;
        return featurerRestDTOMapper.convert(featurerService.findEntitySafe(src.getFeaturerId()), mapperContext);
    }

    public void postpone(Integer featurerId, HashMap<String, String> params, MapperContext mapperContext) {
        postpone(new FeaturerParams(featurerId, params), mapperContext);
    }

    /**
     * Walks the params against the featurer param definitions and postpones an EntityRef for every
     * entity-referencing param value. Cheap: no DB access here, refs are resolved in bulk on the drain phase.
     */
    private void postponeEntityRefs(FeaturerParams src, MapperContext mapperContext) {
        if (src.getFeaturerId() == null || src.getParams() == null || src.getParams().isEmpty())
            return;
        Map<String, FeaturerParam<?>> paramDefinitions = featurerService.getFeaturerParams(src.getFeaturerId());
        if (paramDefinitions.isEmpty())
            return;
        Properties properties = new Properties();
        properties.putAll(src.getParams());
        for (Map.Entry<String, String> param : src.getParams().entrySet()) {
            String value = param.getValue();
            if (value == null || value.isBlank() || value.contains("injection@")) // injections are resolved separately
                continue;
            FeaturerParam<?> paramDefinition = paramDefinitions.get(param.getKey());
            if (paramDefinition == null) // stale hstore key unknown to the featurer
                continue;
            FeaturerParamType paramType = paramDefinition.getClass().getAnnotation(FeaturerParamType.class);
            if (paramType == null)
                continue;
            Class<?> targetClass = paramType.targetEntity();
            if (targetClass == Void.class) // not an entity-referencing param
                continue;
            try {
                Object extracted = paramDefinition.extract(properties);
                if (extracted instanceof UUID uuid)
                    entityRefRestDTOMapper.postpone(new EntityRef(targetClass, uuid), mapperContext);
                else if (extracted instanceof Collection<?> idCollection)
                    for (Object id : idCollection)
                        if (id instanceof UUID uuid)
                            entityRefRestDTOMapper.postpone(new EntityRef(targetClass, uuid), mapperContext);
            } catch (Exception e) { // malformed param value must not fail the endpoint: format is validated on save
                log.warn("Can not extract entity ref from featurer[{}] param[{}]={}: {}",
                        src.getFeaturerId(), param.getKey(), value, e.getMessage());
            }
        }
    }

    @Override
    public void map(FeaturerParams src, FeaturerDTOv1 dst, MapperContext mapperContext) {
        // conversion is fully handled by convert() via FeaturerRestDTOMapper delegation
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(FeaturerMode.HIDE);
    }

    @Override
    public String getObjectCacheId(FeaturerParams src) {
        return src == null ? null : src.cacheKey();
    }
}
