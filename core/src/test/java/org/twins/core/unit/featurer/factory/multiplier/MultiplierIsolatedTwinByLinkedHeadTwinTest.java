package org.twins.core.unit.featurer.factory.multiplier;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.enums.status.StatusType;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.featurer.factory.multiplier.MultiplierIsolatedTwinByLinkedHeadTwin;

import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MultiplierIsolatedTwinByLinkedHeadTwinTest extends BaseUnitTest {

    @Mock
    private TwinRepository twinRepository;

    private MultiplierIsolatedTwinByLinkedHeadTwin multiplier;

    @BeforeEach
    void setUp() {
        multiplier = new MultiplierIsolatedTwinByLinkedHeadTwin(twinRepository);
    }

    private Properties buildProperties(UUID linkId, String statusIdsCsv, boolean excludeStatuses) {
        var props = new Properties();
        props.put("linkId", linkId.toString());
        if (statusIdsCsv != null) {
            props.put("statusIds", statusIdsCsv);
        }
        props.put("excludeStatuses", Boolean.toString(excludeStatuses));
        return props;
    }

    private FactoryItem buildInputItem(UUID twinId) {
        var twin = new TwinEntity()
                .setId(twinId)
                .setTwinStatus(new TwinStatusEntity().setType(StatusType.BASIC));
        var twinUpdate = new TwinUpdate().setDbTwinEntity(twin);
        return new FactoryItem().setOutput(twinUpdate);
    }

    // twins are cloned in the loop; clone triggers isSketch() which reads twinStatus.getType(),
    // so every twin under test must carry a non-null status.
    private TwinEntity buildTwin() {
        var twin = new TwinEntity();
        twin.setId(UUID.randomUUID());
        twin.setTwinStatus(new TwinStatusEntity().setType(StatusType.BASIC));
        return twin;
    }

    private Object[] row(UUID dstInputTwinId, TwinEntity headChild) {
        return new Object[]{dstInputTwinId, headChild};
    }

    @Nested
    class Multiply {

        @Test
        void multiply_linkedHeadTwinsFound_producesOneOutputPerLinkedTwin() throws ServiceException {
            // contract: via link (src=head, dst=input), collect src heads, then output their child twins.
            var linkId = UUID.randomUUID();
            var input1 = UUID.randomUUID();
            var input2 = UUID.randomUUID();
            var props = buildProperties(linkId, null, false);

            var ctx = mock(FactoryContext.class);
            when(ctx.getRunLimitedByOwnerBusinessAccount()).thenReturn(null);

            var headChild1 = buildTwin();
            var headChild2 = buildTwin();
            var headChild3 = buildTwin();
            when(twinRepository.findDstTwinIdAndTwinsByHeadOfLinkSrcTowardDstTwins(
                    eq(List.of(input1, input2)), eq(linkId), isNull()))
                    .thenReturn(List.of(
                            row(input1, headChild1),
                            row(input1, headChild2),
                            row(input2, headChild3)));

            var input = List.of(buildInputItem(input1), buildInputItem(input2));

            var result = multiplier.multiply(props, input, ctx);

            assertEquals(3, result.size());

            var out0 = (TwinUpdate) result.get(0).getOutput();
            assertSame(headChild1, out0.getDbTwinEntity());
            // each output is wired to its single source input
            assertSame(input.get(0), result.get(0).getContextFactoryItemList().get(0));
            assertSame(input.get(1), result.get(2).getContextFactoryItemList().get(0));
        }

        @Test
        void multiply_statusSetProvided_routesToIncludedVariant() throws ServiceException {
            // When statusIds is non-empty and excludeStatuses=false, the Included JPQL variant is used.
            var linkId = UUID.randomUUID();
            var status1 = UUID.randomUUID();
            var status2 = UUID.randomUUID();
            var input1 = UUID.randomUUID();
            var props = buildProperties(linkId, status1 + "," + status2, false);

            var ctx = mock(FactoryContext.class);
            when(ctx.getRunLimitedByOwnerBusinessAccount()).thenReturn(null);

            var twin = buildTwin();
            when(twinRepository.findDstTwinIdAndTwinsByHeadOfLinkSrcTowardDstTwinsStatusesIncluded(
                    anyList(), eq(linkId), isNull(), argThat(s -> s != null && s.contains(status1) && s.contains(status2))))
                    .thenReturn(Collections.singletonList(row(input1, twin)));

            var result = multiplier.multiply(props, List.of(buildInputItem(input1)), ctx);

            assertEquals(1, result.size());
            verify(twinRepository).findDstTwinIdAndTwinsByHeadOfLinkSrcTowardDstTwinsStatusesIncluded(
                    anyList(), any(), any(), any());
            verify(twinRepository, never()).findDstTwinIdAndTwinsByHeadOfLinkSrcTowardDstTwinsStatusesExcluded(
                    anyList(), any(), any(), any());
            verify(twinRepository, never()).findDstTwinIdAndTwinsByHeadOfLinkSrcTowardDstTwins(anyList(), any(), any());
        }

        @Test
        void multiply_statusSetProvided_excludeTrue_routesToExcludedVariant() throws ServiceException {
            var linkId = UUID.randomUUID();
            var status1 = UUID.randomUUID();
            var input1 = UUID.randomUUID();
            var props = buildProperties(linkId, status1.toString(), true);

            var ctx = mock(FactoryContext.class);
            when(ctx.getRunLimitedByOwnerBusinessAccount()).thenReturn(null);

            when(twinRepository.findDstTwinIdAndTwinsByHeadOfLinkSrcTowardDstTwinsStatusesExcluded(
                    anyList(), eq(linkId), isNull(), argThat(s -> s != null && s.contains(status1))))
                    .thenReturn(List.of());

            multiplier.multiply(props, List.of(buildInputItem(input1)), ctx);

            verify(twinRepository).findDstTwinIdAndTwinsByHeadOfLinkSrcTowardDstTwinsStatusesExcluded(
                    anyList(), any(), any(), any());
        }

        @Test
        void multiply_inputWithoutLinkedTwins_skipped() throws ServiceException {
            var linkId = UUID.randomUUID();
            var inputWithResult = UUID.randomUUID();
            var inputWithoutResult = UUID.randomUUID();
            var props = buildProperties(linkId, null, false);

            var ctx = mock(FactoryContext.class);
            when(ctx.getRunLimitedByOwnerBusinessAccount()).thenReturn(null);

            var twin = buildTwin();
            when(twinRepository.findDstTwinIdAndTwinsByHeadOfLinkSrcTowardDstTwins(
                    anyList(), eq(linkId), isNull()))
                    .thenReturn(Collections.singletonList(row(inputWithResult, twin)));

            var input = List.of(buildInputItem(inputWithResult), buildInputItem(inputWithoutResult));

            var result = multiplier.multiply(props, input, ctx);

            assertEquals(1, result.size());
            assertSame(twin, ((TwinUpdate) result.get(0).getOutput()).getDbTwinEntity());
        }

        @Test
        void multiply_emptyInput_skipsRepositoryCall() throws ServiceException {
            var linkId = UUID.randomUUID();
            var props = buildProperties(linkId, null, false);

            var ctx = mock(FactoryContext.class);

            var result = multiplier.multiply(props, List.of(), ctx);

            assertTrue(result.isEmpty());
            verifyNoInteractions(twinRepository);
        }
    }
}
