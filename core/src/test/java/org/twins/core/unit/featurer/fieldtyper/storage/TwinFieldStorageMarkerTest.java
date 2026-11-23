package org.twins.core.unit.featurer.fieldtyper.storage;

import org.cambium.common.exception.ServiceException;
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
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageMarker;
import org.twins.core.service.twin.TwinMarkerService;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

class TwinFieldStorageMarkerTest extends BaseUnitTest {

    @Mock
    private TwinMarkerService twinMarkerService;

    @InjectMocks
    private TwinFieldStorageMarker storage;

    @Test
    void loadDelegatesToTheBulkMarkerLoad() throws ServiceException {
        TwinEntity twin = new TwinEntity().setId(UUID.randomUUID());
        Kit<TwinEntity, UUID> kit = new Kit<>(List.of(twin), TwinEntity::getId);

        storage.load(kit);

        verify(twinMarkerService).loadMarkers(ArgumentMatchers.<Collection<TwinEntity>>argThat(
                twins -> twins.size() == 1 && twins.contains(twin)));
    }

    @Test
    void loadedMeansTheMarkerKitIsSet() {
        TwinEntity twin = new TwinEntity();
        assertFalse(storage.isLoaded(twin));
        twin.setTwinMarkerKit(new Kit<>(List.of(), DataListOptionEntity::getId));
        assertTrue(storage.isLoaded(twin));
    }

    @Test
    void initEmptyFillsAnEmptyOptionKit() {
        TwinEntity twin = new TwinEntity();

        storage.initEmpty(twin);

        assertNotNull(twin.getTwinMarkerKit());
        assertTrue(twin.getTwinMarkerKit().isEmpty());
    }

    @Test
    void countGroupIsADistinctDimension() {
        TwinClassFieldEntity field = new TwinClassFieldEntity().setId(SystemIds.TwinClassField.Base.MARKERS);

        GroupExpressionProvider<TwinEntity> provider = storage.countGroup(field);

        // static twin_marker rows only (dynamic markers are not part of the SQL dimension);
        // a twin with several markers counts once per option group, not once per joined row
        assertNotNull(provider);
        assertTrue(provider.isDistinctGroup());
    }
}
