package org.twins.core.featurer.statistic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinFieldDecimalRepository;
import org.twins.core.dao.twin.TwinFieldHeadSumCountProjection;
import org.twins.core.dao.twin.TwinNoRelationsProjection;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.dao.twin.TwinFieldValueProjection;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class StatisterForParentWithoutSelfPercentTest extends BaseUnitTest {

    @Mock
    private TwinFieldDecimalRepository twinFieldDecimalRepository;

    @Mock
    private TwinRepository twinRepository;

    @InjectMocks
    private StatisterForParentWithoutSelfPercent statister;

    private UUID childFieldId;
    private UUID grandChildFieldId;
    private UUID childClassId;
    private UUID labelI18nId;

    @BeforeEach
    void setUp() {
        childFieldId = UUID.randomUUID();
        grandChildFieldId = UUID.randomUUID();
        childClassId = UUID.randomUUID();
        labelI18nId = UUID.randomUUID();
    }

    private Properties props() {
        var props = new Properties();
        props.put("childTwinClassFieldId", childFieldId.toString());
        props.put("grandChildTwinClassFieldId", grandChildFieldId.toString());
        props.put("ofChildTwinClassIds", childClassId.toString());
        props.put("key", "progress");
        props.put("labelI18nId", labelI18nId.toString());
        props.put("colorHex", "#0000FF");
        return props;
    }

    private TwinNoRelationsProjection childProjection(UUID headTwinId, UUID childId) {
        return new TwinNoRelationsProjection(
                childId, UUID.randomUUID(), headTwinId, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null
        );
    }

    @Nested
    class GetStatistic {

        @Test
        void getStatistic_withGrandchildStats_returnsAvgFromGrandchildren() {
            var parentId = UUID.randomUUID();
            var childId = UUID.randomUUID();

            when(twinRepository.findByHeadTwinIdInAndTwinClassIdIn(any(), any(), eq(TwinNoRelationsProjection.class)))
                    .thenReturn(List.of(childProjection(parentId, childId)));
            when(twinFieldDecimalRepository.sumAndCountByHeadTwinId(any(), eq(grandChildFieldId)))
                    .thenReturn(List.of(new TwinFieldHeadSumCountProjection(childId, new BigDecimal("1.20"), 2L)));

            var result = statister.getStatistic(props(), Set.of(parentId));

            assertEquals(60, result.get(parentId).getItems().getFirst().getPercent());
        }

        @Test
        void getStatistic_noChildren_returnsEmptyMap() {
            var parentId = UUID.randomUUID();

            when(twinRepository.findByHeadTwinIdInAndTwinClassIdIn(any(), any(), eq(TwinNoRelationsProjection.class)))
                    .thenReturn(Collections.emptyList());

            var result = statister.getStatistic(props(), Set.of(parentId));

            assertTrue(result.isEmpty());
        }

        @Test
        void getStatistic_noGrandchildStats_usesChildFieldValue() {
            var parentId = UUID.randomUUID();
            var childId = UUID.randomUUID();

            when(twinRepository.findByHeadTwinIdInAndTwinClassIdIn(any(), any(), eq(TwinNoRelationsProjection.class)))
                    .thenReturn(List.of(childProjection(parentId, childId)));
            when(twinFieldDecimalRepository.sumAndCountByHeadTwinId(any(), eq(grandChildFieldId)))
                    .thenReturn(Collections.emptyList());
            when(twinFieldDecimalRepository.valueByTwinId(any(), eq(childFieldId)))
                    .thenReturn(List.of(new TwinFieldValueProjection(childId, new BigDecimal("0.80"))));

            var result = statister.getStatistic(props(), Set.of(parentId));

            assertEquals(80, result.get(parentId).getItems().getFirst().getPercent());
        }
    }
}
