package org.twins.core.unit.featurer.factory.filler;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.enums.status.StatusType;
import org.twins.core.featurer.factory.filler.FillerMarkerAdd;

import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FillerMarkerAddTest extends BaseUnitTest {

    private final FillerMarkerAdd filler = new FillerMarkerAdd();

    private static final UUID MARKER_ID = UUID.randomUUID();

    private Properties props(boolean hardAdd) {
        var p = new Properties();
        p.setProperty("markerId", MARKER_ID.toString());
        p.setProperty("hardAdd", Boolean.toString(hardAdd));
        return p;
    }

    private FactoryItem buildCreateItem() {
        var output = new TwinCreate();
        output.setTwinEntity(new TwinEntity());
        return new FactoryItem().setOutput(output);
    }

    private FactoryItem buildUpdateItem() {
        // TwinUpdate.setDbTwinEntity -> isSketch -> needs a non-null twinStatus.
        var dbTwin = new TwinEntity().setTwinStatus(new TwinStatusEntity().setType(StatusType.BASIC));
        var twinUpdate = new TwinUpdate();
        twinUpdate.setDbTwinEntity(dbTwin).setTwinEntity(new TwinEntity());
        return new FactoryItem().setOutput(twinUpdate);
    }

    @Nested
    class Fill {

        @Test
        void fill_createOutput_addsMarkerToCreate() throws ServiceException {
            // NAME promises: add marker to output twin.
            var factoryItem = buildCreateItem();

            filler.fill(props(false), List.of(factoryItem), null, false);

            var create = (TwinCreate) factoryItem.getOutput();
            assertNotNull(create.getMarkersAdd());
            assertTrue(create.getMarkersAdd().contains(MARKER_ID));
        }

        @Test
        void fill_updateOutputHardAdd_removesFromMarkersDelete() throws ServiceException {
            // hardAdd=true: if the marker is currently pending deletion, the delete must be undone.
            var factoryItem = buildUpdateItem();
            var update = (TwinUpdate) factoryItem.getOutput();
            update.setMarkersDelete(new java.util.HashSet<>(Set.of(MARKER_ID)));

            filler.fill(props(true), List.of(factoryItem), null, false);

            assertNotNull(update.getMarkersAdd());
            assertTrue(update.getMarkersAdd().contains(MARKER_ID));
            assertFalse(update.getMarkersDelete().contains(MARKER_ID));
        }

        @Test
        void fill_updateOutputNotHardAdd_leavesMarkersDeleteUntouched() throws ServiceException {
            var factoryItem = buildUpdateItem();
            var update = (TwinUpdate) factoryItem.getOutput();
            update.setMarkersDelete(new java.util.HashSet<>(Set.of(MARKER_ID)));

            filler.fill(props(false), List.of(factoryItem), null, false);

            assertTrue(update.getMarkersAdd().contains(MARKER_ID));
            // not hardAdd -> markersDelete still contains the marker (both add+delete recorded)
            assertTrue(update.getMarkersDelete().contains(MARKER_ID));
        }

        @Test
        void fill_updateOutputHardAddNullMarkersDelete_stillAdds() throws ServiceException {
            var factoryItem = buildUpdateItem();
            var update = (TwinUpdate) factoryItem.getOutput();
            assertNull(update.getMarkersDelete());

            filler.fill(props(true), List.of(factoryItem), null, false);

            assertTrue(update.getMarkersAdd().contains(MARKER_ID));
            assertNull(update.getMarkersDelete());
        }
    }
}
