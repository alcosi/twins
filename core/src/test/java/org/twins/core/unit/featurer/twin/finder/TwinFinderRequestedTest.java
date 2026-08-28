package org.twins.core.featurer.twin.finder;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.params.FeaturerParamString;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.domain.search.TwinSearch;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TwinFinderRequestedTest extends BaseUnitTest {

    private final TwinFinderRequested finder = new TwinFinderRequested() {
        @Override
        public void concat(TwinSearch twinSearch, Properties properties, Map<String, String> namedParamsMap) {
        }
    };

    private final FeaturerParamString paramKey = new FeaturerParamString("testParamKey");

    @Nested
    class GetRequestedIds {

        @Test
        void getRequestedIds_singleUuid_returnsSetWithOneElement() throws ServiceException {
            var uuid = UUID.randomUUID();

            var properties = new Properties();
            properties.setProperty("testParamKey", "myKey");
            properties.setProperty("required", "false");
            var namedParams = Map.of("myKey", uuid.toString());

            var result = finder.getRequestedIds(paramKey, properties, namedParams);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertTrue(result.contains(uuid));
        }

        @Test
        void getRequestedIds_multipleUuids_returnsSetWithAllElements() throws ServiceException {
            var uuid1 = UUID.randomUUID();
            var uuid2 = UUID.randomUUID();
            var uuid3 = UUID.randomUUID();

            var properties = new Properties();
            properties.setProperty("testParamKey", "myKey");
            properties.setProperty("required", "false");
            var namedParams = Map.of("myKey", uuid1 + "," + uuid2 + "," + uuid3);

            var result = finder.getRequestedIds(paramKey, properties, namedParams);

            assertNotNull(result);
            assertEquals(3, result.size());
            assertTrue(result.contains(uuid1));
            assertTrue(result.contains(uuid2));
            assertTrue(result.contains(uuid3));
        }

        @Test
        void getRequestedIds_commaSeparatedWithSpaces_trimsCorrectly() throws ServiceException {
            var uuid1 = UUID.randomUUID();
            var uuid2 = UUID.randomUUID();

            var properties = new Properties();
            properties.setProperty("testParamKey", "myKey");
            properties.setProperty("required", "false");
            var namedParams = Map.of("myKey", uuid1 + " , " + uuid2);

            var result = finder.getRequestedIds(paramKey, properties, namedParams);

            assertNotNull(result);
            assertEquals(2, result.size());
            assertTrue(result.contains(uuid1));
            assertTrue(result.contains(uuid2));
        }

        @Test
        void getRequestedIds_blankValueRequired_throwsParamMissed() {
            var properties = new Properties();
            properties.setProperty("testParamKey", "myKey");
            properties.setProperty("required", "true");
            var namedParams = Map.of("myKey", "");

            var ex = assertThrows(
                    ServiceException.class,
                    () -> finder.getRequestedIds(paramKey, properties, namedParams)
            );
            assertEquals(ErrorCodeTwins.TWIN_SEARCH_PARAM_MISSED.getCode(), ex.getErrorCode());
        }

        @Test
        void getRequestedIds_missingValueRequired_throwsParamMissed() {
            var properties = new Properties();
            properties.setProperty("testParamKey", "myKey");
            properties.setProperty("required", "true");

            var ex = assertThrows(
                    ServiceException.class,
                    () -> finder.getRequestedIds(paramKey, properties, Map.of())
            );
            assertEquals(ErrorCodeTwins.TWIN_SEARCH_PARAM_MISSED.getCode(), ex.getErrorCode());
        }

        @Test
        void getRequestedIds_blankValueNotRequired_returnsNull() throws ServiceException {
            var properties = new Properties();
            properties.setProperty("testParamKey", "myKey");
            properties.setProperty("required", "false");
            var namedParams = Map.of("myKey", "");

            var result = finder.getRequestedIds(paramKey, properties, namedParams);

            assertNull(result);
        }

        @Test
        void getRequestedIds_missingValueNotRequired_returnsNull() throws ServiceException {
            var properties = new Properties();
            properties.setProperty("testParamKey", "myKey");
            properties.setProperty("required", "false");

            var result = finder.getRequestedIds(paramKey, properties, Map.of());

            assertNull(result);
        }

        @Test
        void getRequestedIds_invalidUuid_throwsConfigIncorrect() {
            var properties = new Properties();
            properties.setProperty("testParamKey", "myKey");
            properties.setProperty("required", "false");
            var namedParams = Map.of("myKey", "not-a-uuid");

            var ex = assertThrows(
                    ServiceException.class,
                    () -> finder.getRequestedIds(paramKey, properties, namedParams)
            );
            assertEquals(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT.getCode(), ex.getErrorCode());
        }

        @Test
        void getRequestedIds_oneValidOneInvalidUuid_throwsConfigIncorrect() {
            var uuid = UUID.randomUUID();

            var properties = new Properties();
            properties.setProperty("testParamKey", "myKey");
            properties.setProperty("required", "false");
            var namedParams = Map.of("myKey", uuid + ",not-a-uuid");

            var ex = assertThrows(
                    ServiceException.class,
                    () -> finder.getRequestedIds(paramKey, properties, namedParams)
            );
            assertEquals(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT.getCode(), ex.getErrorCode());
        }
    }

    @Nested
    class GetRequestedId {

        @Test
        void getRequestedId_validUuid_returnsUuid() throws ServiceException {
            var uuid = UUID.randomUUID();

            var properties = new Properties();
            properties.setProperty("testParamKey", "myKey");
            properties.setProperty("required", "false");
            var namedParams = Map.of("myKey", uuid.toString());

            var result = finder.getRequestedId(paramKey, properties, namedParams);

            assertEquals(uuid, result);
        }

        @Test
        void getRequestedId_blankValueRequired_throwsParamMissed() {
            var properties = new Properties();
            properties.setProperty("testParamKey", "myKey");
            properties.setProperty("required", "true");
            var namedParams = Map.of("myKey", "");

            var ex = assertThrows(
                    ServiceException.class,
                    () -> finder.getRequestedId(paramKey, properties, namedParams)
            );
            assertEquals(ErrorCodeTwins.TWIN_SEARCH_PARAM_MISSED.getCode(), ex.getErrorCode());
        }

        @Test
        void getRequestedId_missingValueRequired_throwsParamMissed() {
            var properties = new Properties();
            properties.setProperty("testParamKey", "myKey");
            properties.setProperty("required", "true");

            var ex = assertThrows(
                    ServiceException.class,
                    () -> finder.getRequestedId(paramKey, properties, Map.of())
            );
            assertEquals(ErrorCodeTwins.TWIN_SEARCH_PARAM_MISSED.getCode(), ex.getErrorCode());
        }

        @Test
        void getRequestedId_blankValueNotRequired_returnsNull() throws ServiceException {
            var properties = new Properties();
            properties.setProperty("testParamKey", "myKey");
            properties.setProperty("required", "false");
            var namedParams = Map.of("myKey", "");

            var result = finder.getRequestedId(paramKey, properties, namedParams);

            assertNull(result);
        }

        @Test
        void getRequestedId_missingValueNotRequired_returnsNull() throws ServiceException {
            var properties = new Properties();
            properties.setProperty("testParamKey", "myKey");
            properties.setProperty("required", "false");

            var result = finder.getRequestedId(paramKey, properties, Map.of());

            assertNull(result);
        }

        @Test
        void getRequestedId_invalidUuid_throwsConfigIncorrect() {
            var properties = new Properties();
            properties.setProperty("testParamKey", "myKey");
            properties.setProperty("required", "false");
            var namedParams = Map.of("myKey", "not-a-uuid");

            var ex = assertThrows(
                    ServiceException.class,
                    () -> finder.getRequestedId(paramKey, properties, namedParams)
            );
            assertEquals(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT.getCode(), ex.getErrorCode());
        }
    }

    @Nested
    class Constants {

        @Test
        void paramConstants_haveExpectedValues() {
            assertEquals("twinId", TwinFinderRequested.PARAM_TWIN_ID);
            assertEquals("userId", TwinFinderRequested.PARAM_USER_ID);
            assertEquals("twinClassId", TwinFinderRequested.PARAM_TWIN_CLASS_ID);
            assertEquals("linkId", TwinFinderRequested.PARAM_LINK_ID);
            assertEquals("statusId", TwinFinderRequested.PARAM_STATUS_ID);
        }
    }
}
