package org.twins.core.featurer.classfield.projector;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.HashMap;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@Disabled("Projector returns null - implementation pending")
class FieldProjectorStringToDataListV1Test extends BaseUnitTest {

    @Mock
    private FeaturerService featurerService;

    private FieldProjectorStringToDataListV1 projector;

    @BeforeEach
    void setUp() {
        projector = new FieldProjectorStringToDataListV1();
        projector.featurerService = featurerService;
    }

    @Nested
    class Project {

        @Test
        void project_withParams_returnsNull() throws ServiceException {
            var params = new HashMap<String, String>();
            var field = new TwinClassFieldEntity();
            var entity = new TwinEntity();
            var properties = new Properties();
            when(featurerService.extractProperties(projector, params)).thenReturn(properties);

            var result = projector.project(params, field, entity);

            assertNull(result);
            verify(featurerService).extractProperties(projector, params);
        }

        @Test
        void project_withEmptyParams_returnsNull() throws ServiceException {
            var params = new HashMap<String, String>();
            var field = new TwinClassFieldEntity();
            var entity = new TwinEntity();
            when(featurerService.extractProperties(projector, params)).thenReturn(new Properties());

            var result = projector.project(params, field, entity);

            assertNull(result);
        }
    }
}
