package org.twins.core.unit.service.twin;

import org.cambium.common.kit.KitGrouped;
import org.junit.jupiter.api.Test;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldDataListEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageDatalist;
import org.twins.core.service.twin.TwinGroupKeyValues;
import org.twins.core.service.twinlink.TwinLinkService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class TwinGroupKeyValuesTest {
    @Test
    void dataListWithoutARowIsTheNullKeyAndRepeatedOptionsCollapse() throws Exception {
        UUID fieldId = UUID.fromString("95f2c8ca-8b79-4971-9726-270a11c7d031");
        UUID option = UUID.randomUUID();
        TwinClassFieldEntity field = new TwinClassFieldEntity().setId(fieldId);
        TwinFieldStorageDatalist storage = mock(TwinFieldStorageDatalist.class);
        TwinEntity empty = new TwinEntity().setId(UUID.randomUUID());
        assertEquals(Collections.singletonList(null), TwinGroupKeyValues.dynamic(empty, field, storage));

        TwinEntity task = new TwinEntity().setId(UUID.randomUUID());
        task.setTwinFieldDatalistKit(new KitGrouped<>(
                List.of(
                        row(fieldId, option),
                        row(fieldId, option),
                        row(fieldId, null)),
                TwinFieldDataListEntity::getId,
                TwinFieldDataListEntity::getTwinClassFieldId));
        assertEquals(Arrays.asList(option, null), TwinGroupKeyValues.dynamic(task, field, storage));
    }

    @Test
    void forwardLinkFieldUsesEachDstOnceAndAMissingLinkIsTheNullKey() {
        UUID linkId = UUID.randomUUID();
        UUID dst = UUID.randomUUID();
        TwinLinkService.FindTwinLinksResult links = new TwinLinkService.FindTwinLinksResult();
        links.getForwardLinks().add(new TwinLinkEntity().setId(UUID.randomUUID()).setLinkId(linkId).setDstTwinId(dst));
        links.getForwardLinks().add(new TwinLinkEntity().setId(UUID.randomUUID()).setLinkId(linkId).setDstTwinId(dst));
        assertEquals(List.of(dst), TwinGroupKeyValues.linkEnds(links, linkId, true));
        assertEquals(Collections.singletonList(null), TwinGroupKeyValues.linkEnds(links, UUID.randomUUID(), true));
        assertEquals(Collections.singletonList(null), TwinGroupKeyValues.linkEnds(null, linkId, true));
    }

    private static TwinFieldDataListEntity row(UUID fieldId, UUID optionId) {
        return new TwinFieldDataListEntity()
                .setId(UUID.randomUUID())
                .setTwinClassFieldId(fieldId)
                .setDataListOptionId(optionId);
    }
}
