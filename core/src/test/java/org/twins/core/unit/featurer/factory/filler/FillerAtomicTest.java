package org.twins.core.unit.featurer.factory.filler;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.factory.FactoryItemsBatch;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.factory.filler.FillerAtomic;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Batch loop contract of {@link FillerAtomic}: beforeFill runs once before any item, every item is
 * attempted, a failing item of an optional step does not abort the rest of the batch, a failing item
 * of a mandatory step aborts it (fail-fast).
 */
class FillerAtomicTest extends BaseUnitTest {

    static class FakeAtomicFiller extends FillerAtomic {
        int filledCount = 0;
        final FactoryItem failingItem;
        final List<String> events = new ArrayList<>();

        FakeAtomicFiller(FactoryItem failingItem) {
            this.failingItem = failingItem;
        }

        @Override
        protected void beforeFill(FactoryItemsBatch batch, TwinEntity templateTwin) {
            events.add("beforeFill");
        }

        @Override
        public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
            events.add("item");
            filledCount++;
            if (factoryItem == failingItem) {
                throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "boom");
            }
        }
    }

    private FactoryItem buildFactoryItem() {
        var output = new TwinCreate();
        output.setTwinEntity(new TwinEntity());
        return new FactoryItem().setOutput(output);
    }

    @Test
    void failingItemOfOptionalStepDoesNotAbortBatch() throws Exception {
        var item1 = buildFactoryItem();
        var item2 = buildFactoryItem();
        var item3 = buildFactoryItem();
        var filler = new FakeAtomicFiller(item2);

        filler.fill(new Properties(), new FactoryItemsBatch().add(item1).add(item2).add(item3), null, true);

        assertEquals(3, filler.filledCount); // loop continued past the failure
        assertEquals(List.of("beforeFill", "item", "item", "item"), filler.events); // preload once, then every item
    }

    @Test
    void failingItemOfMandatoryStepAbortsBatchFailFast() {
        var item1 = buildFactoryItem();
        var item2 = buildFactoryItem();
        var item3 = buildFactoryItem();
        var filler = new FakeAtomicFiller(item2);

        assertThrows(ServiceException.class,
                () -> filler.fill(new Properties(), new FactoryItemsBatch().add(item1).add(item2).add(item3), null, false));

        assertEquals(2, filler.filledCount); // item3 was never attempted
        assertEquals(List.of("beforeFill", "item", "item"), filler.events);
    }

    @Test
    void emptyBatchIsNoOp() throws Exception {
        var filler = new FakeAtomicFiller(null);

        filler.fill(new Properties(), new FactoryItemsBatch(), null, true);
        filler.fill(new Properties(), null, null, false);

        assertEquals(0, filler.filledCount);
        assertEquals(List.of(), filler.events);
    }

    @Test
    void batchDerivesTwinsAndTwinIds() {
        var item1 = buildFactoryItem();
        item1.getOutput().getTwinEntity().setId(java.util.UUID.randomUUID());
        var item2 = buildFactoryItem();

        var batch = new FactoryItemsBatch().add(item1).add(item2).add(item1); // duplicate add ignored

        assertEquals(2, batch.size());
        assertEquals(List.of(item1.getOutput().getTwinEntity(), item2.getOutput().getTwinEntity()), batch.getTwins());
        assertEquals(1, batch.getTwinIds().size()); // only item1 has an id
    }

    @Test
    void sharedContextTwinIsReturnedOnce_noMatterHowManyItemsReferenceIt() {
        // multiplier copies share ONE input item as their context — its twin must be returned
        // once, not once per referencing item
        var sharedContextOutput = new TwinCreate();
        var sharedContextTwin = new TwinEntity();
        sharedContextOutput.setTwinEntity(sharedContextTwin);
        var sharedContextItem = new FactoryItem().setOutput(sharedContextOutput);
        var item1 = buildFactoryItem().setContextFactoryItemList(List.of(sharedContextItem));
        var item2 = buildFactoryItem().setContextFactoryItemList(List.of(sharedContextItem));
        var item3 = buildFactoryItem().setContextFactoryItemList(List.of(sharedContextItem));

        var batch = new FactoryItemsBatch().add(item1).add(item2).add(item3);

        assertEquals(Set.of(sharedContextTwin), batch.getContextTwins()); // one entry, same instance
    }

    @Test
    void contextTwinsDefaultToLevel1_andDeeperLevelIsExplicit() {
        // level 2 (context item's own context twin) is what the "look deeper" step of
        // FieldLookuperFromContextFieldsAndContextTwinDbFields reads — everyone else stays on level 1
        // and must not pay for loading it
        var level2Output = new TwinCreate();
        var level2Twin = new TwinEntity().setId(java.util.UUID.randomUUID()); // distinct ids: field-based TwinEntity.equals treats two bare twins as equal
        level2Output.setTwinEntity(level2Twin);
        var level2Item = new FactoryItem().setOutput(level2Output);
        var level1Output = new TwinCreate();
        var level1Twin = new TwinEntity().setId(java.util.UUID.randomUUID());
        level1Output.setTwinEntity(level1Twin);
        var level1Item = new FactoryItem().setOutput(level1Output).setContextFactoryItemList(List.of(level2Item));
        var item = buildFactoryItem().setContextFactoryItemList(List.of(level1Item));

        var batch = new FactoryItemsBatch().add(item);

        assertEquals(Set.of(level1Twin), batch.getContextTwins());      // default = level 1 only
        assertEquals(Set.of(level1Twin, level2Twin), batch.getContextTwins(2));
    }
}
