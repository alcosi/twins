package org.cambium.featurer;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.annotations.FeaturerParamType;
import org.cambium.featurer.annotations.FeaturerType;
import org.cambium.featurer.dao.*;
import org.cambium.featurer.exception.ErrorCodeFeaturer;
import org.cambium.featurer.injectors.Injector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

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
    Hashtable<Integer, Set<String>> featurerParamsMap = new Hashtable<>();

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
                syncFeaturerType(featurerTypeAnnotation);
                FeaturerEntity featurerEntity = new FeaturerEntity();
                featurerEntity.setId(featurerAnnotation.id());
                featurerEntity.setName(StringUtils.isNotBlank(featurerAnnotation.name()) ? featurerAnnotation.name() : featurerClass.getSimpleName());
                featurerEntity.setClazz(featurerClass.getName());
                featurerEntity.setFeaturerTypeId(featurerTypeAnnotation.id());
                featurerEntity.setDescription(featurerAnnotation.description());
                featurerRepository.save(featurerEntity);
                featurerMap.put(featurerAnnotation.id(), featurer);
                syncFeaturersParams(featurerClass);
            } catch (Exception e) {
                log.error("Got exception: ", e);
            }
        }
    }

    private static Set<FeaturerType> syncedFeaturerTypes = new HashSet<>();

    private void syncFeaturerType(FeaturerType featurerTypeAnnotation) {
        if (syncedFeaturerTypes.add(featurerTypeAnnotation)) {
            FeaturerTypeEntity featurerTypeEntity = new FeaturerTypeEntity();
            featurerTypeEntity.setId(featurerTypeAnnotation.id());
            featurerTypeEntity.setName(featurerTypeAnnotation.name());
            featurerTypeEntity.setDescription(featurerTypeAnnotation.description());
            featurerTypeRepository.save(featurerTypeEntity);
        }
    }

    private void syncFeaturersParams(Class<Featurer> featurerClass) {
        org.cambium.featurer.annotations.Featurer featurerAnnotation = featurerClass.getAnnotation(org.cambium.featurer.annotations.Featurer.class);
        Set<String> featurerParamsKeySet = new HashSet<>();
        for (Field field : featurerClass.getFields()) {
            try {
                FeaturerParam featurerParamAnnotation = field.getAnnotation(FeaturerParam.class);
                if (featurerParamAnnotation != null) {
                    FeaturerParamType featurerParamTypeAnnotation = field.getType().getAnnotation(FeaturerParamType.class);
                    if (featurerParamTypeAnnotation == null) {
                        log.error("FeaturerParamType is not specified for param[{}]!", field.getType().getSimpleName());
                        continue;
                    }
                    syncFeaturerParamType(featurerParamTypeAnnotation);
                    FeaturerParamEntity featurerParamEntity = new FeaturerParamEntity();
                    featurerParamEntity.setFeaturerId(featurerAnnotation.id());
                    //для доступа к key, важно чтобы поле было public static final
                    String key = ((org.cambium.featurer.params.FeaturerParam) field.get(null)).getKey();
                    featurerParamEntity.setKey(key);
                    featurerParamEntity.setName(featurerParamAnnotation.name());
                    featurerParamEntity.setDescription(featurerParamAnnotation.description());
                    featurerParamEntity.setOrder(featurerParamAnnotation.order());
                    featurerParamEntity.setFeaturerParamTypeId(featurerParamTypeAnnotation.id());
                    featurerParamRepository.save(featurerParamEntity);
                    featurerParamsKeySet.add(key);
                }
            } catch (IllegalAccessException e) {
                log.error("Exception: ", e);
            }
        }
        featurerParamsMap.put(featurerAnnotation.id(), featurerParamsKeySet);
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

    public <T extends Featurer> T getFeaturer(FeaturerEntity featurerEntity, Class<T> featurerType) throws ServiceException {
        Featurer featurer = featurerMap.get(featurerEntity.getId());
        if (featurer == null)
            throw new ServiceException(ErrorCodeFeaturer.INCORRECT_CONFIGURATION, "Can not load feature with id " + featurerEntity.getId());
        if (!featurerType.isInstance(featurer)) {
            throw new ServiceException(ErrorCodeFeaturer.INCORRECT_CONFIGURATION, String.format("Feature %s can not be loaded as %s", featurerEntity.getId(), featurerType.getSimpleName()));
        }
        org.cambium.featurer.annotations.Featurer annotation = ClassUtils.getUserClass(featurer.getClass()).getAnnotation(org.cambium.featurer.annotations.Featurer.class);
        if (annotation.id() != featurerEntity.getId())
            throw new ServiceException(ErrorCodeFeaturer.INCORRECT_CONFIGURATION, "Incorrect featurer component id " + featurerEntity.getId());
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

    public void loadAllFeaturerFieldsParams(Object object) {
        Method[] methods = object.getClass().getMethods();
        for (Method method : methods) {
            if (method.getReturnType().equals(FeaturerEntity.class)) {
                try {
                    FeaturerEntity featurerEntity = (FeaturerEntity) method.invoke(object);
                    featurerEntity.setParams(featurerParamRepository.findByFeaturer(featurerEntity));
                } catch (Exception e) {
                    log.error("Exception: ", e);
                }
            }
        }
    }

    public Properties extractProperties(FeaturerEntity featurerEntity, HashMap<String, String> params, HashMap<String, Object> context) throws ServiceException {
        return extractProperties(featurerEntity.getId(), params, context);
    }

    public Properties extractProperties(Featurer featurer, HashMap<String, String> params, HashMap<String, Object> context) throws ServiceException {
        org.cambium.featurer.annotations.Featurer annotation = featurer.getClass().getAnnotation(org.cambium.featurer.annotations.Featurer.class);
        return extractProperties(annotation.id(), params, context);
    }

    public Properties extractProperties(Integer featurerId, HashMap<String, String> params, HashMap<String, Object> context) throws ServiceException {
        Properties ret = new Properties();
        Set<String> keySet = featurerParamsMap.get(featurerId);
        int paramsCount = params != null ? params.size() : 0;
        if (paramsCount != keySet.size())
            throw new ServiceException(ErrorCodeFeaturer.INCORRECT_CONFIGURATION,
                    String.format("Incorrect params count for featurer[%s]. Expected %s, got %s", featurerId, keySet.size(), paramsCount));
        if (paramsCount == 0)
            return ret;//no params
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!featurerParamsMap.get(featurerId).contains(entry.getKey()))
                throw new ServiceException(ErrorCodeFeaturer.INCORRECT_CONFIGURATION,
                        String.format("Unknown params key[%s] for featurer[%s]", entry.getKey(), featurerId));
            if (entry.getValue().contains("injection@")) {
                try {
                    ret.put(entry.getKey(), extractInjectedProperties(UUID.fromString(StringUtils.substringAfter(entry.getValue(), "@")), context));
                } catch (Exception e) {
                    log.error("error getting value[" + entry.getValue() + "] injected by key[" + entry.getKey() + "]", e);
                    ret.put(entry.getKey(), entry.getValue());
                }
            } else {
                ret.put(entry.getKey(), entry.getValue());
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
    public String extractInjectedProperties(UUID injectionId, HashMap<String, Object> context) throws Exception {
        FeaturerInjectionEntity injection = injectionRepository.findById(injectionId).orElseThrow(NullPointerException::new);
        return getInjector(injection.getInjectorFeaturer()).doInject(injection, context);
    }

    public Injector getInjector(FeaturerEntity featurerEntity) throws ServiceException {
        return getFeaturer(featurerEntity, Injector.class);
    }
}
