package org.twins.core.featurer.twin.validator;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkRepository;

import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class TwinValidatorTwinChildrenFieldSumInStatusesPositiveTest extends BaseUnitTest {

    @Mock
    private TwinLinkRepository twinLinkRepository;

    private TwinValidatorTwinChildrenFieldSumInStatusesPositive validator;

    @BeforeEach
    void setUp() {
        validator = new TwinValidatorTwinChildrenFieldSumInStatusesPositive(twinLinkRepository);
    }

    @Nested
    class IsValidCollection {

        @Test
        void isValid_hasLinkedSrcWithPositiveField_returnsValid() throws ServiceException {
            var linkId = UUID.randomUUID();
            var statusId = UUID.randomUUID();
            var fieldId = UUID.randomUUID();
            var twinId = UUID.randomUUID();
            var twin = new TwinEntity();
            twin.setId(twinId);

            when(twinLinkRepository.findDstTwinIdsLinkedFromSrcWithStatusAndPositiveDecimalField(
                    Set.of(twinId), linkId, Set.of(statusId), fieldId))
                    .thenReturn(Set.of(twinId));

            var props = new Properties();
            props.put("linkId", linkId.toString());
            props.put("statusIds", statusId.toString());
            props.put("twinClassFieldId", fieldId.toString());

            var result = validator.isValid(props, List.of(twin), false);

            assertTrue(result.getTwinsResults().get(twinId).isValid());
        }

        @Test
        void isValid_noLinkedSrcWithPositiveField_returnsInvalid() throws ServiceException {
            var linkId = UUID.randomUUID();
            var statusId = UUID.randomUUID();
            var fieldId = UUID.randomUUID();
            var twinId = UUID.randomUUID();
            var twin = new TwinEntity();
            twin.setId(twinId);

            when(twinLinkRepository.findDstTwinIdsLinkedFromSrcWithStatusAndPositiveDecimalField(
                    Set.of(twinId), linkId, Set.of(statusId), fieldId))
                    .thenReturn(Collections.emptySet());

            var props = new Properties();
            props.put("linkId", linkId.toString());
            props.put("statusIds", statusId.toString());
            props.put("twinClassFieldId", fieldId.toString());

            var result = validator.isValid(props, List.of(twin), false);

            assertFalse(result.getTwinsResults().get(twinId).isValid());
        }

        @Test
        void isValid_emptyCollection_returnsEmptyResult() throws ServiceException {
            var linkId = UUID.randomUUID();
            var statusId = UUID.randomUUID();
            var fieldId = UUID.randomUUID();

            var props = new Properties();
            props.put("linkId", linkId.toString());
            props.put("statusIds", statusId.toString());
            props.put("twinClassFieldId", fieldId.toString());

            var result = validator.isValid(props, Collections.emptyList(), false);

            assertTrue(result.getTwinsResults().isEmpty());
        }

        @Test
        void isValid_nullTwinId_returnsInvalid() throws ServiceException {
            var linkId = UUID.randomUUID();
            var statusId = UUID.randomUUID();
            var fieldId = UUID.randomUUID();
            var twin = new TwinEntity();
            twin.setId(null);

            var props = new Properties();
            props.put("linkId", linkId.toString());
            props.put("statusIds", statusId.toString());
            props.put("twinClassFieldId", fieldId.toString());

            var result = validator.isValid(props, List.of(twin), false);

            // twin with null id is filtered out of dstTwinIds, but still gets a result with null key
            assertTrue(result.getTwinsResults().containsKey(null));
            assertFalse(result.getTwinsResults().get(null).isValid());
        }

        @Test
        void isValid_multipleNullTwinIds_onlyOneResultKey() throws ServiceException {
            var linkId = UUID.randomUUID();
            var statusId = UUID.randomUUID();
            var fieldId = UUID.randomUUID();
            var twin1 = new TwinEntity();
            twin1.setId(null);
            var twin2 = new TwinEntity();
            twin2.setId(null);
            var twin3 = new TwinEntity();
            twin3.setId(null);

            var props = new Properties();
            props.put("linkId", linkId.toString());
            props.put("statusIds", statusId.toString());
            props.put("twinClassFieldId", fieldId.toString());

            var result = validator.isValid(props, List.of(twin1, twin2, twin3), false);

            // Bug: multiple null-id twins overwrite each other in result map
            // Only one entry with null key exists, data loss for other twins
            assertEquals(1, result.getTwinsResults().size(),
                    "Only one result entry should exist for multiple null-id twins (demonstrates data loss bug)");
            assertTrue(result.getTwinsResults().containsKey(null),
                    "Result should have null key");
            // The last null twin's result overwrites previous ones
        }

        @Test
        void isValid_hasLinkedSrcWithPositiveField_inverted_returnsInvalid() throws ServiceException {
            var linkId = UUID.randomUUID();
            var statusId = UUID.randomUUID();
            var fieldId = UUID.randomUUID();
            var twinId = UUID.randomUUID();
            var twin = new TwinEntity();
            twin.setId(twinId);

            when(twinLinkRepository.findDstTwinIdsLinkedFromSrcWithStatusAndPositiveDecimalField(
                    Set.of(twinId), linkId, Set.of(statusId), fieldId))
                    .thenReturn(Set.of(twinId));

            var props = new Properties();
            props.put("linkId", linkId.toString());
            props.put("statusIds", statusId.toString());
            props.put("twinClassFieldId", fieldId.toString());

            var result = validator.isValid(props, List.of(twin), true);

            assertFalse(result.getTwinsResults().get(twinId).isValid());
        }
    }
}
