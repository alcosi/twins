package org.twins.core.featurer.twin.finder;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.params.FeaturerParamString;
import org.junit.jupiter.api.Test;
import org.twins.core.exception.ErrorCodeTwins;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TwinFinderByFieldDateRequestedTest {
    private static final FeaturerParamString KEY = new FeaturerParamString("lessThenOrEqualsParamKey");

    @Test
    void parsesIsoValue() throws ServiceException {
        Map<String, String> params = new HashMap<>();
        params.put("until", "2026-10-03T00:00:00");
        assertEquals(LocalDateTime.of(2026, 10, 3, 0, 0),
                TwinFinderByFieldDateRequested.resolveBound(KEY, properties("lessThenOrEqualsParamKey", "until"), params));
    }

    @Test
    void requiredParamMissedFails() {
        // defaults from @FeaturerParam are only filled by FeaturerService.extractProperties, set explicitly
        Properties properties = properties("lessThenOrEqualsParamKey", "until", "required", "true");
        ServiceException se = assertThrows(ServiceException.class,
                () -> TwinFinderByFieldDateRequested.resolveBound(KEY, properties, new HashMap<>()));
        assertEquals(ErrorCodeTwins.TWIN_SEARCH_PARAM_MISSED.getCode(), se.getErrorCode());
    }

    @Test
    void optionalParamMissedYieldsNull() throws ServiceException {
        Properties properties = properties("lessThenOrEqualsParamKey", "until", "required", "false");
        assertNull(TwinFinderByFieldDateRequested.resolveBound(KEY, properties, new HashMap<>()));
    }

    @Test
    void unconfiguredKeyYieldsNull() throws ServiceException {
        assertNull(TwinFinderByFieldDateRequested.resolveBound(KEY, new Properties(), new HashMap<>()));
    }

    @Test
    void malformedValueFails() {
        Map<String, String> params = new HashMap<>();
        params.put("until", "03.10.2026");
        ServiceException se = assertThrows(ServiceException.class,
                () -> TwinFinderByFieldDateRequested.resolveBound(KEY, properties("lessThenOrEqualsParamKey", "until"), params));
        assertEquals(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT.getCode(), se.getErrorCode());
    }

    private static Properties properties(String... keyValuePairs) {
        Properties properties = new Properties();
        for (int i = 0; i < keyValuePairs.length; i += 2)
            properties.setProperty(keyValuePairs[i], keyValuePairs[i + 1]);
        return properties;
    }
}
