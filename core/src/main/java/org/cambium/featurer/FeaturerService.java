package org.cambium.featurer;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.CollectionUtils;
import org.cambium.common.util.KitUtils;
import org.cambium.common.util.MapUtils;
import org.cambium.common.util.PaginationUtils;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.annotations.FeaturerParamType;
import org.cambium.featurer.annotations.FeaturerType;
import org.cambium.featurer.dao.*;
import org.cambium.featurer.exception.ErrorCodeFeaturer;
import org.cambium.featurer.injectors.Injector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.twins.core.domain.search.FeaturerSearch;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.cambium.featurer.dao.specifications.FeaturerSpecification.checkIntegerIn;
import static org.springframework.data.jpa.domain.Specification.allOf;
import static org.twins.core.dao.specifications.CommonSpecification.checkFieldLikeIn;

@Component
@Slf4j
@RequiredArgsConstructor
public class FeaturerService {
    final FeaturerRepository featurerRepository;
    final FeaturerTypeRepository featurerTypeRepository;
    final FeaturerParamRepository featurerParamRepository;
    final FeaturerParamTypeRepository featurerParamTypeRepository;
    final FeaturerInjectionRepository injectionRepository;
    List<Featurer> featurerList;
    Hashtable<Integer, Featurer> featurerMap = new Hashtable<>();
    Kit<FeaturerEntity, Integer> featurerEntityKit = new Kit<>(FeaturerEntity::getId);
    Hashtable<Integer, Map<String, FeaturerParam>> featurerParamsAnnotationsMap = new Hashtable<>();
    Hashtable<Integer, Map<String, org.cambium.featurer.params.FeaturerParam<?>>> featurerParamsMap = new Hashtable<>();

    @Autowired //lazy loading because of circular dependency
    public void setFeaturerList(List<Featurer> featurerList) {
        this.featurerList = featurerList;
    }

    @PostConstruct
    public void postConstruct() {
        syncFeaturers();
        // cleaning
        syncedFeaturerTypes = null;
        syncedFeaturerParamTypes = null;
    }

    private void syncFeaturers() {
        log.info("syncFeaturers: started");
        List<FeaturerTypeEntity> featurerTypeEntityList = new ArrayList<>();
        List<FeaturerParamEntity> featurerParamEntityList = new ArrayList<>();
        for (Featurer featurer : featurerList) {
            try {
                // т.к объекты созданы spring, то класс может содержать не все аннотации
                Class<Featurer> featurerClass = (Class<Featurer>) ClassUtils.getUserClass(featurer.getClass());
                org.cambium.featurer.annotations.Featurer featurerAnnotation = featurerClass.getAnnotation(org.cambium.featurer.annotations.Featurer.class);
                FeaturerType featurerTypeAnnotation = featurerClass.getAnnotation(FeaturerType.class);
                if (featurerTypeAnnotation == null) {
                    log.error("FeaturerType is not specified for class[{}]!", featurerClass.getSimpleName());
                    continue;
                }
                syncFeaturerType(featurerTypeAnnotation, featurerTypeEntityList);
                FeaturerEntity featurerEntity = new FeaturerEntity();
                featurerEntity.setId(featurerAnnotation.id());
                featurerEntity.setName(StringUtils.isNotBlank(featurerAnnotation.name()) ? featurerAnnotation.name() : featurerClass.getSimpleName());
                featurerEntity.setClazz(featurerClass.getName());
                featurerEntity.setFeaturerTypeId(featurerTypeAnnotation.id());
                featurerEntity.setDescription(featurerAnnotation.description());
                Deprecated deprecatedAnnotation = featurerClass.getAnnotation(Deprecated.class);
                if (deprecatedAnnotation != null)
                    featurerEntity.setDeprecated(true);
                featurerEntityKit.add(featurerEntity);
                featurerMap.put(featurerAnnotation.id(), featurer);
                syncFeaturersParams(featurerClass, featurerParamEntityList);
            } catch (Exception e) {
                log.error("Got exception: ", e);
            }
        }
        featurerTypeRepository.saveAll(featurerTypeEntityList);
        featurerRepository.saveAll(featurerEntityKit.getCollection());
        //truncating old params
        featurerParamRepository.deleteAllByFeaturerIdIn(featurerEntityKit.getIdSet());
        featurerParamRepository.saveAll(featurerParamEntityList);
        log.info("syncFeaturers: ended");
    }

    private static Set<FeaturerType> syncedFeaturerTypes = new HashSet<>();

    private void syncFeaturerType(FeaturerType featurerTypeAnnotation, List<FeaturerTypeEntity> featurerTypeEntityList) {
        if (syncedFeaturerTypes.add(featurerTypeAnnotation)) {
            FeaturerTypeEntity featurerTypeEntity = new FeaturerTypeEntity();
            featurerTypeEntity.setId(featurerTypeAnnotation.id());
            featurerTypeEntity.setName(featurerTypeAnnotation.name());
            featurerTypeEntity.setDescription(featurerTypeAnnotation.description());
            featurerTypeEntityList.add(featurerTypeEntity);
        }
    }

    private void syncFeaturersParams(Class<Featurer> featurerClass, List<FeaturerParamEntity> featurerParamEntityList) {
        org.cambium.featurer.annotations.Featurer featurerAnnotation = featurerClass.getAnnotation(org.cambium.featurer.annotations.Featurer.class);
        Map<String, org.cambium.featurer.params.FeaturerParam<?>> featurerParamsMap = new HashMap<>();
        Map<String, FeaturerParam> featurerParamsAnnotationMap = new HashMap<>();
        for (Field field : featurerClass.getFields()) {
            try {
                FeaturerParam featurerParamAnnotation = field.getAnnotation(FeaturerParam.class);
                if (featurerParamAnnotation != null) {
                    FeaturerParamType featurerParamTypeAnnotation = field.get(null).getClass().getAnnotation(FeaturerParamType.class);
                    if (featurerParamTypeAnnotation == null) {
                        log.error("FeaturerParamType is not specified for param[{}]!", field.getType().getSimpleName());
                        continue;
                    }
                    syncFeaturerParamType(featurerParamTypeAnnotation);
                    FeaturerParamEntity featurerParamEntity = new FeaturerParamEntity();
                    featurerParamEntity.setFeaturerId(featurerAnnotation.id());
                    //для доступа к key, важно чтобы поле было public static final
                    var instance = (org.cambium.featurer.params.FeaturerParam) field.get(null);
                    String key = instance.getKey();
                    featurerParamEntity.setKey(key);
                    featurerParamEntity.setName(featurerParamAnnotation.name());
                    featurerParamEntity.setDescription(featurerParamAnnotation.description());
                    featurerParamEntity.setOrder(featurerParamAnnotation.order());
                    featurerParamEntity.setFeaturerParamTypeId(featurerParamTypeAnnotation.id());
                    featurerParamEntity.setOptional(featurerParamAnnotation.optional());
                    featurerParamEntity.setDefaultValue(!Objects.equals(featurerParamAnnotation.defaultValue(), FeaturerParam.DEFAULT_VALUE_NOT_SET) ? featurerParamAnnotation.defaultValue() : null);
                    featurerParamEntity.setExampleValues(featurerParamAnnotation.exampleValues().length > 0 ? featurerParamAnnotation.exampleValues() : null);
                    featurerParamEntityList.add(featurerParamEntity);
                    featurerParamsMap.put(key, instance);
                    featurerParamsAnnotationMap.put(key, featurerParamAnnotation);
                }
            } catch (IllegalAccessException e) {
                log.error("Exception: ", e);
            }
        }
        this.featurerParamsMap.put(featurerAnnotation.id(), featurerParamsMap);
        this.featurerParamsAnnotationsMap.put(featurerAnnotation.id(), featurerParamsAnnotationMap);
    }


    private static Set<FeaturerParamType> syncedFeaturerParamTypes = new HashSet<>();

    private void syncFeaturerParamType(FeaturerParamType featurerParamTypeAnnotation) {
        if (syncedFeaturerParamTypes.add(featurerParamTypeAnnotation)) {
            FeaturerParamTypeEntity featurerParamTypeEntity = new FeaturerParamTypeEntity();
            featurerParamTypeEntity.setDescription(featurerParamTypeAnnotation.description());
            featurerParamTypeEntity.setExample(featurerParamTypeAnnotation.example());
            featurerParamTypeEntity.setRegexp(featurerParamTypeAnnotation.regexp());
            featurerParamTypeEntity.setId(featurerParamTypeAnnotation.id());
            featurerParamTypeRepository.save(featurerParamTypeEntity);
        }
    }

    @Deprecated
    public <T extends Featurer> T getFeaturer(FeaturerEntity featurerEntity, Class<T> featurerType) throws ServiceException {
        if (featurerEntity == null)
            throw new ServiceException(ErrorCodeCommon.FEATURER_IS_NULL);
        return getFeaturer(featurerEntity.getId(), featurerType);
    }

    public <T extends Featurer> T getFeaturer(Integer featurerId, Class<T> featurerType) throws ServiceException {
        if (featurerId == null)
            throw new ServiceException(ErrorCodeCommon.FEATURER_IS_NULL);
        Featurer featurer = featurerMap.get(featurerId);
        if (featurer == null)
            throw new ServiceException(ErrorCodeFeaturer.INCORRECT_CONFIGURATION, "Can not load featurer with id " + featurerId);
        if (!featurerType.isInstance(featurer)) {
            throw new ServiceException(ErrorCodeFeaturer.INCORRECT_CONFIGURATION, String.format("Featurer %s can not be loaded as %s", featurerId, featurerType.getSimpleName()));
        }
        org.cambium.featurer.annotations.Featurer annotation = ClassUtils.getUserClass(featurer.getClass()).getAnnotation(org.cambium.featurer.annotations.Featurer.class);
        if (annotation.id() != featurerId)
            throw new ServiceException(ErrorCodeFeaturer.INCORRECT_CONFIGURATION, "Incorrect featurer component id " + featurerId);
        return (T) featurer;
    }

    @Cacheable("FeaturerEntityListCache")
    public List<FeaturerEntity> getFeaturerEntityList(Class<? extends Featurer> type) {
        FeaturerType featurerTypeAnnotation = type.getAnnotation(FeaturerType.class);
        List<FeaturerEntity> list = featurerRepository.findByFeaturerTypeId(featurerTypeAnnotation.id());
        for (FeaturerEntity featurerEntity : list) {
            featurerEntity.setParams(featurerParamRepository.findByFeaturer(featurerEntity));
        }
        return list;
    }

    @Cacheable("FeaturerParamEntity")
    public FeaturerParamEntity getFeaturerParamEntity(Integer id, String key) {
        return featurerParamRepository.findByFeaturerIdAndKey(id, key);
    }

    public void loadFeaturerParams(FeaturerEntity featurerEntity) {
        loadFeaturerParams(Collections.singleton(featurerEntity));
    }

    public void loadFeaturerParams(Collection<FeaturerEntity> featurerEntityCollection) {
        if (CollectionUtils.isEmpty(featurerEntityCollection))
            return;
        Kit<FeaturerEntity, Integer> needLoad = new Kit<>(FeaturerEntity::getId);
        for (FeaturerEntity featurerEntity : featurerEntityCollection) {
            if (featurerEntity.getParams() == null)
                needLoad.add(featurerEntity);
        }
        if (CollectionUtils.isEmpty(needLoad))
            return;
        List<FeaturerParamEntity> allParams = featurerParamRepository.findByFeaturerIdIn(needLoad.getIdSet());
        if (CollectionUtils.isEmpty(allParams))
            return;
        Map<Integer, List<FeaturerParamEntity>> paramsGroupedByFeaturerId = allParams.stream()
                .collect(Collectors.groupingBy(FeaturerParamEntity::getFeaturerId));
        for (FeaturerEntity featurerEntity : needLoad.getCollection()) {
            List<FeaturerParamEntity> params = paramsGroupedByFeaturerId.get(featurerEntity.getId());
            featurerEntity.setParams(params != null ? params.stream()
                    .sorted(Comparator.comparingInt(FeaturerParamEntity::getOrder))
                    .collect(Collectors.toList()) : Collections.EMPTY_LIST);
        }
    }

    public void loadAllFeaturerFieldsParams(Object object) {
        Method[] methods = object.getClass().getMethods();
        for (Method method : methods) {
            if (method.getReturnType().equals(FeaturerEntity.class)) {
                try {
                    FeaturerEntity featurerEntity = (FeaturerEntity) method.invoke(object);
                    if (featurerEntity.getParams() == null)
                        featurerEntity.setParams(featurerParamRepository.findByFeaturer(featurerEntity));
                } catch (Exception e) {
                    log.error("Exception: ", e);
                }
            }
        }
    }

    public Properties extractProperties(FeaturerEntity featurerEntity, HashMap<String, String> params, Map<String, Object> context) throws ServiceException {
        return extractProperties(featurerEntity.getId(), params);
    }

    public Properties extractProperties(Featurer featurer, HashMap<String, String> params, Map<String, Object> context) throws ServiceException {
        return extractProperties(getFeaturerId(featurer), params);
    }

    private int getFeaturerId(Featurer featurer) {
        var annotation = featurer.getClass().getAnnotation(org.cambium.featurer.annotations.Featurer.class);
        return annotation.id();
    }

    public Properties extractProperties(Integer featurerId, HashMap<String, String> params, Map<String, Object> context) throws ServiceException {
        Properties ret = new Properties();

        var paramsAnnotationsMap = featurerParamsAnnotationsMap.get(featurerId);

        params = params != null ? params : new HashMap<>();

        int paramsCount = (int) params.values().stream().filter(Objects::nonNull).count();
        int notOptionalParamsCountSetting = (int) paramsAnnotationsMap.values().stream().filter(it -> !it.optional()).count();
        int totalParamsCountSetting = paramsAnnotationsMap.values().size();

        if (paramsCount < notOptionalParamsCountSetting) {
            throw new ServiceException(ErrorCodeFeaturer.INCORRECT_CONFIGURATION, String.format("Incorrect params count for featurer[%s]. Expected (%s,%s), got %s", featurerId, notOptionalParamsCountSetting, totalParamsCountSetting, paramsCount));
        }

        for (var entry : paramsAnnotationsMap.entrySet()) {
            String value = null;
            if (params.get(entry.getKey()) != null) {
                value = params.get(entry.getKey());
            } else if (!Objects.equals(entry.getValue().defaultValue(), FeaturerParam.DEFAULT_VALUE_NOT_SET)) {
                value = entry.getValue().defaultValue();
            } else if (!entry.getValue().optional()) {
                throw new ServiceException(ErrorCodeFeaturer.INCORRECT_CONFIGURATION, String.format("Incorrect non-optional param[%s] value[null] for featurer[%s].", entry.getKey(), featurerId));
            }
            if (value != null && value.contains("injection@")) {
                try {
                    ret.put(entry.getKey(), extractInjectedProperties(UUID.fromString(StringUtils.substringAfter(value, "@")), context));
                } catch (Exception e) {
                    log.error("error getting value[" + entry.getValue() + "] injected by key[" + entry.getKey() + "]", e);
                    ret.put(entry.getKey(), value);
                }
            } else if (value != null) {
                ret.put(entry.getKey(), value);
            }
        }
        return ret;
    }

    /**
     * @param injectionId - идентификатор инъекции
     * @param context     - необходимые данные для заполнения его значения
     *                    Заполняет properties, значения которых не содержатся напрямую в бд в соответствующем столбце
     *                    ..._params, а подставляются исходя из условий injections_conditions из других таблиц
     * @return значение параметра, которое было получено через инъекцию
     */
    public String extractInjectedProperties(UUID injectionId, Map<String, Object> context) throws Exception {
        FeaturerInjectionEntity injection = injectionRepository.findById(injectionId).orElseThrow(NullPointerException::new);
        return getInjector(injection.getInjectorFeaturer()).doInject(injection, context);
    }

    public Injector getInjector(FeaturerEntity featurerEntity) throws ServiceException {
        return getFeaturer(featurerEntity, Injector.class);
    }

    public PaginationResult<FeaturerEntity> findFeaturers(FeaturerSearch featurerSearch, SimplePagination pagination) throws ServiceException {
        Specification<FeaturerEntity> spec = createFeaturerSearchSpecification(featurerSearch);
        Page<FeaturerEntity> ret = featurerRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return PaginationUtils.convertInPaginationResult(ret, pagination);
    }

    public Specification<FeaturerEntity> createFeaturerSearchSpecification(FeaturerSearch featurerSearch) {
        return allOf(
                checkIntegerIn(FeaturerEntity.Fields.id, featurerSearch.getIdList(), false),
                checkIntegerIn(FeaturerEntity.Fields.featurerTypeId, featurerSearch.getTypeIdList(), false),
                checkFieldLikeIn(featurerSearch.getNameLikeList(), false, true, FeaturerEntity.Fields.name));
    }

    public FeaturerEntity checkValid(Integer featurerId, HashMap<String, String> featurerParams, Class<? extends Featurer> expectedFeaturerClass) throws ServiceException {
        Featurer featurer = featurerMap.get(featurerId);
        if (featurer == null)
            throw new ServiceException(ErrorCodeCommon.FEATURER_ID_UNKNOWN, "unknown featurer id[" + featurerId + "]");
        if (!expectedFeaturerClass.isInstance(featurer))
            throw new ServiceException(ErrorCodeCommon.FEATURER_INCORRECT_TYPE, "featurer of id[" + featurerId + "] is not of expected type[" + expectedFeaturerClass.getSimpleName() + "]");
        Properties properties = extractProperties(featurer, featurerParams, new HashMap<>());
        basicParamsValidation(featurerId, properties);
        featurer.extraParamsValidation(properties);
        return featurerRepository.findById(featurerId).get();
    }

    public void basicParamsValidation(Integer featurerId, Properties properties) throws ServiceException {
        var paramsMap = featurerParamsMap.get(featurerId);
        for (var entry : paramsMap.entrySet()) {
            if (properties.getProperty(entry.getKey()) != null) {
                entry.getValue().validate(properties.getProperty(entry.getKey()));
            }
        }
    }

    public FeaturerEntity getFeaturerEntity(Integer featurerId) {
        return featurerRepository.findById(featurerId).get();
    }

    public Kit<FeaturerEntity, Integer> findEntitiesSafe(Set<Integer> ids) {
        Kit<FeaturerEntity, Integer> ret = new Kit<>(FeaturerEntity::getId);
        for (var id : ids) {
            ret.add(featurerEntityKit.get(id));
        }
        return ret;
    }

    public <E, K> void loadFeaturers(Collection<E> srcCollection,
                                     Function<? super E, ? extends K> functionGetId,
                                     Function<? super E, Integer> functionGetFeaturerId,
                                     Function<? super E, FeaturerEntity> functionGetFeaturerEntity,
                                     BiConsumer<E, FeaturerEntity> functionSetFeaturerEntity) {
        if (srcCollection.isEmpty()) {
            return;
        }
        KitGrouped<E, K, Integer> needLoad = KitUtils.createNeedLoadGrouped(srcCollection, functionGetId, functionGetFeaturerId, functionGetFeaturerEntity);
        if (KitUtils.isEmpty(needLoad)) {
            return;
        }
        Kit<FeaturerEntity, Integer> featurers = findEntitiesSafe(needLoad.getGroupedKeySet());
        int featurerId = 0;
        for (var item : needLoad) {
            featurerId = functionGetFeaturerId.apply(item);
            functionSetFeaturerEntity.accept(item, featurers.get(featurerId));
        }
    }

    public HashMap<String, String> prepareForStore(Integer featurerId, HashMap<String, String> featurerParams) throws ServiceException {
        if (null == featurerId) return null;
        Featurer featurer = featurerMap.get(featurerId);
        if (featurer == null)
            throw new ServiceException(ErrorCodeCommon.FEATURER_ID_UNKNOWN, "unknown featurer id[" + featurerId + "]");
        var paramsMap = featurerParamsMap.get(featurerId);
        for (var entry : paramsMap.entrySet()) {
            var key = entry.getKey();
            var value = featurerParams.get(key);
            var preparedForStoreValue = entry.getValue().prepareForStore(value);
            if (!Objects.equals(value, preparedForStoreValue)) {
                featurerParams.put(key, preparedForStoreValue);
            }
        }
        return featurerParams;
    }

    @Cacheable(value = "FeaturerExtractPropertiesCache", key = "#featurerId + '|' + T(org.apache.commons.codec.digest.DigestUtils).sha256Hex(#featurerId + '|' + canonical(#params))")
    public Properties extractProperties(Integer featurerId, HashMap<String, String> params) throws ServiceException {
        return extractProperties(featurerId, params, Collections.emptyMap());
    }

    private String canonical(Map<String, String> map) {
        if(MapUtils.isEmpty(map)) return "";
        return map.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));
    }
}
