package org.twins.core.featurer.twin.finder;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamString;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public abstract class TwinFinderRequested extends TwinFinder {
    public static final String PARAM_TWIN_ID = "twinId";
    public static final String PARAM_USER_ID = "userId";
    public static final String PARAM_TWIN_CLASS_ID = "twinClassId";
    public static final String PARAM_LINK_ID = "linkId";
    public static final String PARAM_STATUS_ID = "statusId";

    @FeaturerParam(name = "Required", description = "", order = 10, optional = true, defaultValue = "true")
    public static final FeaturerParamBoolean required = new FeaturerParamBoolean("required");

    public Set<UUID> getRequestedIds(FeaturerParamString paramKey, Properties properties, Map<String, String> namedParamsMap) throws ServiceException {
        String paramKeyStr = paramKey.extract(properties);
        String paramValue = namedParamsMap.get(paramKeyStr);
        if (StringUtils.isBlank(paramValue))
            if (required.extract(properties))
                throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_PARAM_MISSED, "search param[" + paramKeyStr + "] missed");
            else
                return null;
        Set<UUID> ret = null;
        try {
            ret = Arrays.stream(paramValue.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(UUID::fromString)
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT, "search param[" + paramKeyStr + "] is not uuid (or uuid list)");
        }
        return ret;
    }

    public UUID getRequestedId(FeaturerParamString paramKey, Properties properties, Map<String, String> namedParamsMap) throws ServiceException {
        String paramKeyStr = paramKey.extract(properties);
        String paramValue = namedParamsMap.get(paramKeyStr);
        if (StringUtils.isBlank(paramValue))
            if (required.extract(properties))
                throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_PARAM_MISSED, "search param[" + paramKeyStr + "] missed");
            else
                return null;
        UUID ret = null;
        try {
            ret = UUID.fromString(paramValue);
        } catch (Exception e) {
            throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT, "search param[" + paramKeyStr + "] is not uuid (or uuid list)");
        }
        return ret;
    }
}
