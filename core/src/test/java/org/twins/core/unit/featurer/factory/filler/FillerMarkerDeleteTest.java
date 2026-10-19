package org.twins.core.unit.featurer.factory.filler;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.enums.status.StatusType;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.featurer.factory.filler.FillerMarkerDelete;

import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FillerMarkerDeleteTest extends BaseUnitTest {

    private final FillerMarkerDelete filler = new FillerMarkerDelete();

    private static final UUID MARKER_ID = UUID.randomUUID();

    private Properties props(boolean soft) {
        var p = new Properties();
        p.setProperty("markerId", MARKER_ID.toString());
        p.setProperty("softDelete", Boolean.toString(soft));
        return p;
    }

    private FactoryItem buildCreateItem() {
        var output = new TwinCreate();
        output.setTwinEntity(new TwinEntity());
        return new FactoryItem().setOutput(output);
    }

    private FactoryItem buildUpdateItem() {
        var dbTwin = new TwinEntity().setTwinStatus(new TwinStatusEntity().setType(StatusType.BASIC));
        var twinUpdate = new TwinUpdate();
        twinUpdate.setDbTwinEntity(dbTwin).setTwinEntity(new TwinEntity());
        return new FactoryItem().setOutput(twinUpdate);
    }

    @Nested
    class Fill {

        @Test
        void fill_createOutput_isNoOp() throws ServiceException {
            // NAME/delete only applies to TwinUpdate; TwinCreate has nothing to delete from.
            var factoryItem = buildCreateItem();

            filler.fill(props(false), factoryItem, null);

            // TwinCreate carries no markersDelete (that field is TwinUpdate-only); no-op = fill
            // completes and the output stays an untouched TwinCreate.
            assertInstanceOf(TwinCreate.class, factoryItem.getOutput());
        }

        @Test
        void fill_updateHardDelete_addsToMarkersDelete() throws ServiceException {
            var factoryItem = buildUpdateItem();
            var update = (TwinUpdate) factoryItem.getOutput();

            filler.fill(props(false), factoryItem, null);

            assertNotNull(update.getMarkersDelete());
            assertTrue(update.getMarkersDelete().contains(MARKER_ID));
        }

        @Test
        void fill_updateSoftDelete_notInMarkersAdd_addsToMarkersDelete() throws ServiceException {
            // soft=true but the marker is not in the pending-add set -> still delete.
            var factoryItem = buildUpdateItem();
            var update = (TwinUpdate) factoryItem.getOutput();

            filler.fill(props(true), factoryItem, null);

            assertTrue(update.getMarkersDelete().contains(MARKER_ID));
        }

        @Test
        void fill_updateSoftDelete_inMarkersAdd_skipsDelete() throws ServiceException {
            // soft=true and the marker is pending add -> cancelling the add takes precedence (no delete recorded).
            var factoryItem = buildUpdateItem();
            var update = (TwinUpdate) factoryItem.getOutput();
            update.setMarkersAdd(new java.util.HashSet<>(Set.of(MARKER_ID)));

            filler.fill(props(true), factoryItem, null);

            // soft-delete of a pending-add marker is a no-op: nothing in markersDelete
            assertNull(update.getMarkersDelete());
        }
    }
}
