package org.twins.core.unit.featurer.fieldtyper.storage;

import org.cambium.common.kit.Kit;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.specifications.GroupExpressionProvider;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.enums.consts.SystemIds;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageTag;
import org.twins.core.service.twin.TwinTagService;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

class TwinFieldStorageTagTest extends BaseUnitTest {

    @Mock
    private TwinTagService twinTagService;

    @InjectMocks
    private TwinFieldStorageTag storage;

    @Test
    void loadDelegatesToTheBulkTagLoad() {
        TwinEntity twin = new TwinEntity().setId(UUID.randomUUID());
        Kit<TwinEntity, UUID> kit = new Kit<>(List.of(twin), TwinEntity::getId);

        storage.load(kit);

        verify(twinTagService).loadTags(ArgumentMatchers.<Collection<TwinEntity>>argThat(
                twins -> twins.size() == 1 && twins.contains(twin)));
    }

    @Test
    void loadedMeansTheTagKitIsSet() {
        TwinEntity twin = new TwinEntity();
        assertFalse(storage.isLoaded(twin));
        twin.setTwinTagKit(new Kit<>(List.of(), DataListOptionEntity::getId));
        assertTrue(storage.isLoaded(twin));
    }

    @Test
    void initEmptyFillsAnEmptyOptionKit() {
        TwinEntity twin = new TwinEntity();

        storage.initEmpty(twin);

        assertNotNull(twin.getTwinTagKit());
        assertTrue(twin.getTwinTagKit().isEmpty());
    }

    @Test
    void countGroupIsADistinctDimension() {
        TwinClassFieldEntity field = new TwinClassFieldEntity().setId(SystemIds.TwinClassField.Base.TAGS);

        GroupExpressionProvider<TwinEntity> provider = storage.countGroup(field);

        // a twin with several tags counts once per option group, not once per joined row
        assertNotNull(provider);
        assertTrue(provider.isDistinctGroup());
    }
}
