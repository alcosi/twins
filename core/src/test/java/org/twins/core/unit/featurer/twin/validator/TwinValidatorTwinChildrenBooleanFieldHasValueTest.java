package org.twins.core.featurer.twin.validator;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.featurer.fieldtyper.FieldTyper;
import org.twins.core.service.twin.TwinSearchService;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class TwinValidatorTwinChildrenBooleanFieldHasValueTest extends BaseUnitTest {

    @Mock
    private TwinSearchService twinSearchService;

    @Mock
    private TwinClassFieldService twinClassFieldService;

    @Mock
    private FeaturerService featurerService;

    private TwinValidatorTwinChildrenBooleanFieldHasValue validator;

    @BeforeEach
    void setUp() {
        validator = new TwinValidatorTwinChildrenBooleanFieldHasValue(twinSearchService, twinClassFieldService);
        validator.featurerService = featurerService;
    }

    @Nested
    class IsValid {

        @Test
        void isValid_hasChildrenWithFieldValue_returnsValid() throws ServiceException {
            var fieldId = UUID.randomUUID();
            var twinId = UUID.randomUUID();
            var twin = new TwinEntity();
            twin.setId(twinId);

            var fieldEntity = new TwinClassFieldEntity();
            fieldEntity.setFieldTyperFeaturerId(1);
            when(twinClassFieldService.findEntitySafe(fieldId)).thenReturn(fieldEntity);
            when(featurerService.getFeaturer(any(Integer.class), eq(FieldTyper.class))).thenReturn(mock(FieldTyper.class));
            when(twinSearchService.countGroupBy(any(BasicSearch.class), eq(TwinEntity.Fields.headTwinId)))
                    .thenReturn(Map.of(twinId, 3L));

            var props = new Properties();
            props.put("twinClassFieldId", fieldId.toString());
            props.put("value", "true");

            var result = validator.isValid(props, List.of(twin), false);

            assertTrue(result.getTwinsResults().get(twinId).isValid());
        }

        @Test
        void isValid_noChildrenWithFieldValue_returnsInvalid() throws ServiceException {
            var fieldId = UUID.randomUUID();
            var twinId = UUID.randomUUID();
            var twin = new TwinEntity();
            twin.setId(twinId);

            var fieldEntity = new TwinClassFieldEntity();
            fieldEntity.setFieldTyperFeaturerId(1);
            when(twinClassFieldService.findEntitySafe(fieldId)).thenReturn(fieldEntity);
            when(featurerService.getFeaturer(any(Integer.class), eq(FieldTyper.class))).thenReturn(mock(FieldTyper.class));
            when(twinSearchService.countGroupBy(any(BasicSearch.class), eq(TwinEntity.Fields.headTwinId)))
                    .thenReturn(Collections.emptyMap());

            var props = new Properties();
            props.put("twinClassFieldId", fieldId.toString());
            props.put("value", "true");

            var result = validator.isValid(props, List.of(twin), false);

            assertFalse(result.getTwinsResults().get(twinId).isValid());
        }

        @Test
        void isValid_hasChildrenWithFieldValue_inverted_returnsInvalid() throws ServiceException {
            var fieldId = UUID.randomUUID();
            var twinId = UUID.randomUUID();
            var twin = new TwinEntity();
            twin.setId(twinId);

            var fieldEntity = new TwinClassFieldEntity();
            fieldEntity.setFieldTyperFeaturerId(1);
            when(twinClassFieldService.findEntitySafe(fieldId)).thenReturn(fieldEntity);
            when(featurerService.getFeaturer(any(Integer.class), eq(FieldTyper.class))).thenReturn(mock(FieldTyper.class));
            when(twinSearchService.countGroupBy(any(BasicSearch.class), eq(TwinEntity.Fields.headTwinId)))
                    .thenReturn(Map.of(twinId, 3L));

            var props = new Properties();
            props.put("twinClassFieldId", fieldId.toString());
            props.put("value", "true");

            var result = validator.isValid(props, List.of(twin), true);

            assertFalse(result.getTwinsResults().get(twinId).isValid());
        }
    }
}
