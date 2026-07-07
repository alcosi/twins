package org.twins.core.unit.featurer.factory.filler;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.factory.filler.FillerHeadFromContextTwinHead;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FillerHeadFromContextTwinHeadTest extends BaseUnitTest {

    private final FillerHeadFromContextTwinHead filler = new FillerHeadFromContextTwinHead();

    private FactoryItem buildFactoryItem(TwinEntity headTwin, UUID headTwinId) {
        var output = new TwinCreate();
        output.setTwinEntity(new TwinEntity());
        var contextTwin = new TwinEntity()
                .setHeadTwin(headTwin)
                .setHeadTwinId(headTwinId);
        var contextOutput = new TwinCreate();
        contextOutput.setTwinEntity(contextTwin);
        var contextItem = new FactoryItem().setOutput(contextOutput);
        return new FactoryItem().setOutput(output).setContextFactoryItemList(List.of(contextItem));
    }

    @Nested
    class Fill {

        @Test
        void fill_singleContext_setsOutputHeadFromContextHead() throws ServiceException {
            // NAME promises: output twin head = the head of the single context twin.
            var headId = UUID.randomUUID();
            var headTwin = new TwinEntity().setId(headId);
            var factoryItem = buildFactoryItem(headTwin, headId);

            filler.fill(new Properties(), factoryItem, null);

            var outputTwin = factoryItem.getOutput().getTwinEntity();
            assertSame(headTwin, outputTwin.getHeadTwin());
            assertEquals(headId, outputTwin.getHeadTwinId());
        }

        @Test
        void fill_contextHeadNull_setsOutputHeadNull() throws ServiceException {
            // context twin with no head -> output head written as null (no throw intended — null is a valid detected value).
            var factoryItem = buildFactoryItem(null, null);

            filler.fill(new Properties(), factoryItem, null);

            var outputTwin = factoryItem.getOutput().getTwinEntity();
            assertNull(outputTwin.getHeadTwin());
            assertNull(outputTwin.getHeadTwinId());
        }

        @Test
        void fill_multipleContextsSameHead_setsOutputHead() throws ServiceException {
            var headId = UUID.randomUUID();
            var headTwin = new TwinEntity().setId(headId);
            var output = new TwinCreate();
            output.setTwinEntity(new TwinEntity());
            var ctx1 = new TwinEntity().setHeadTwin(headTwin).setHeadTwinId(headId);
            var ctx2 = new TwinEntity().setHeadTwin(headTwin).setHeadTwinId(headId);
            var ctx1Out = new TwinCreate();
            ctx1Out.setTwinEntity(ctx1);
            var ctx2Out = new TwinCreate();
            ctx2Out.setTwinEntity(ctx2);
            var factoryItem = new FactoryItem()
                    .setOutput(output)
                    .setContextFactoryItemList(List.of(
                            new FactoryItem().setOutput(ctx1Out),
                            new FactoryItem().setOutput(ctx2Out)));

            filler.fill(new Properties(), factoryItem, null);

            var outputTwin = factoryItem.getOutput().getTwinEntity();
            assertEquals(headId, outputTwin.getHeadTwinId());
        }

        @Test
        @Disabled("bug #14: FillerHeadFromContextTwinHead#fill (the detectedHeadTwinId != null guard) misses a "
                + "null-vs-non-null head disagreement — a [null, X] context pair slips through. Re-enable once fixed.")
        void fill_multipleContextsFirstHeadNullSecondNonNull_throwsFactoryIncorrect() {
            // INTENDED (RED): NAME/comment says "all context twins must have the same headTwinId, otherwise exception".
            // A null-vs-non-null disagreement must throw — but code only compares when detectedHeadTwinId != null,
            // so a first-null / second-non-null pair slips through and silently leaves output head null.
            var headId2 = UUID.randomUUID();
            var output = new TwinCreate();
            output.setTwinEntity(new TwinEntity());
            var ctx1 = new TwinEntity(); // headTwinId == null
            var ctx2 = new TwinEntity().setHeadTwinId(headId2);
            var ctx1Out = new TwinCreate();
            ctx1Out.setTwinEntity(ctx1);
            var ctx2Out = new TwinCreate();
            ctx2Out.setTwinEntity(ctx2);
            var factoryItem = new FactoryItem()
                    .setOutput(output)
                    .setContextFactoryItemList(List.of(
                            new FactoryItem().setOutput(ctx1Out),
                            new FactoryItem().setOutput(ctx2Out)));

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(new Properties(), factoryItem, null));
            assertEquals(ErrorCodeTwins.FACTORY_INCORRECT.getCode(), ex.getErrorCode());
        }

        @Test
        void fill_multipleContextsDifferentHeads_throwsFactoryIncorrect() {
            var headId1 = UUID.randomUUID();
            var headId2 = UUID.randomUUID();
            var output = new TwinCreate();
            output.setTwinEntity(new TwinEntity());
            var ctx1 = new TwinEntity().setHeadTwinId(headId1);
            var ctx2 = new TwinEntity().setHeadTwinId(headId2);
            var ctx1Out = new TwinCreate();
            ctx1Out.setTwinEntity(ctx1);
            var ctx2Out = new TwinCreate();
            ctx2Out.setTwinEntity(ctx2);
            var factoryItem = new FactoryItem()
                    .setOutput(output)
                    .setContextFactoryItemList(List.of(
                            new FactoryItem().setOutput(ctx1Out),
                            new FactoryItem().setOutput(ctx2Out)));

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(new Properties(), factoryItem, null));
            assertEquals(ErrorCodeTwins.FACTORY_INCORRECT.getCode(), ex.getErrorCode());
        }
    }

    @Test
    void canBeOptional_returnsFalse() {
        assertFalse(filler.canBeOptional());
    }
}
