package org.twins.core.unit.featurer.factory.filler;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.LTreeUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.factory.filler.FillerHeadFromContextTwinHead;
import org.twins.core.service.twin.TwinService;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FillerHeadFromContextTwinHeadTest extends BaseUnitTest {

    @Mock
    private TwinService twinService;

    private FillerHeadFromContextTwinHead filler;

    @BeforeEach
    void setUp() throws Exception {
        filler = new FillerHeadFromContextTwinHead();
        inject(filler, "twinService", twinService);
    }

    private void inject(Object target, String name, Object value) throws Exception {
        Field f = findField(target.getClass(), name);
        f.setAccessible(true);
        f.set(target, value);
    }

    private Field findField(Class<?> clazz, String name) {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new RuntimeException("field not found: " + name);
    }

    /**
     * depth is a required runtime param for this featurer (read from properties by the "depth" key).
     */
    private static Properties props(int depth) {
        var properties = new Properties();
        properties.setProperty("depth", Integer.toString(depth));
        return properties;
    }

    /**
     * Builds a factory item whose context twins carry the given ltree hierarchy strings
     * (root-first, as produced by {@link LTreeUtils#convertToChainLTreeFormat}).
     */
    private static FactoryItem buildFactoryItem(String... contextHierarchyTrees) {
        var output = new TwinCreate();
        output.setTwinEntity(new TwinEntity());
        var contextItems = new ArrayList<FactoryItem>();
        for (String tree : contextHierarchyTrees) {
            var contextOutput = new TwinCreate();
            contextOutput.setTwinEntity(new TwinEntity().setHierarchyTree(tree));
            contextItems.add(new FactoryItem().setOutput(contextOutput));
        }
        return new FactoryItem().setOutput(output).setContextFactoryItemList(contextItems);
    }

    @Nested
    class Fill {

        @Test
        void fill_singleContext_setsOutputHeadFromContextHierarchy() throws ServiceException {
            // NAME promises: output twin head = the head resolved from the context twin's hierarchy at the given depth.
            var headId = UUID.randomUUID();
            var contextTwinId = UUID.randomUUID();
            var headTwin = new TwinEntity().setId(headId);
            // depth 1 on "head.context" walks one level up -> head
            var factoryItem = buildFactoryItem(LTreeUtils.convertToChainLTreeFormat(headId, contextTwinId));
            when(twinService.findHeadTwin(headId)).thenReturn(headTwin);

            filler.fill(props(1), factoryItem, null);

            var outputTwin = factoryItem.getOutput().getTwinEntity();
            assertSame(headTwin, outputTwin.getHeadTwin());
            assertEquals(headId, outputTwin.getHeadTwinId());
            verify(twinService).findHeadTwin(headId);
        }

        @Test
        void fill_contextHierarchyBlank_setsOutputHeadNullAndSkipsLookup() throws ServiceException {
            // context twin with no hierarchy -> nothing resolved -> output head null, no DB lookup.
            var factoryItem = buildFactoryItem((String) null);

            filler.fill(props(1), factoryItem, null);

            var outputTwin = factoryItem.getOutput().getTwinEntity();
            assertNull(outputTwin.getHeadTwin());
            assertNull(outputTwin.getHeadTwinId());
            verifyNoInteractions(twinService);
        }

        @Test
        void fill_depthWalksUpMultipleLevels_resolvesHeadHigherInHierarchy() throws ServiceException {
            // hierarchy "head.mid.context", depth 2 walks two levels up -> head (proves depth is honored,
            // not hardcoded to one level).
            var headId = UUID.randomUUID();
            var midId = UUID.randomUUID();
            var contextTwinId = UUID.randomUUID();
            var headTwin = new TwinEntity().setId(headId);
            var factoryItem = buildFactoryItem(LTreeUtils.convertToChainLTreeFormat(headId, midId, contextTwinId));
            when(twinService.findHeadTwin(headId)).thenReturn(headTwin);

            filler.fill(props(2), factoryItem, null);

            assertEquals(headId, factoryItem.getOutput().getTwinEntity().getHeadTwinId());
            verify(twinService).findHeadTwin(headId);
        }

        @Test
        void fill_multipleContextsSameResolvedHead_setsOutputHead() throws ServiceException {
            var headId = UUID.randomUUID();
            var headTwin = new TwinEntity().setId(headId);
            var factoryItem = buildFactoryItem(
                    LTreeUtils.convertToChainLTreeFormat(headId, UUID.randomUUID()),
                    LTreeUtils.convertToChainLTreeFormat(headId, UUID.randomUUID()));
            when(twinService.findHeadTwin(headId)).thenReturn(headTwin);

            filler.fill(props(1), factoryItem, null);

            var outputTwin = factoryItem.getOutput().getTwinEntity();
            assertEquals(headId, outputTwin.getHeadTwinId());
            verify(twinService).findHeadTwin(headId);
        }

        @Test
        void fill_multipleContextsDifferentResolvedHeads_throwsFactoryIncorrect() {
            var headId1 = UUID.randomUUID();
            var headId2 = UUID.randomUUID();
            var factoryItem = buildFactoryItem(
                    LTreeUtils.convertToChainLTreeFormat(headId1, UUID.randomUUID()),
                    LTreeUtils.convertToChainLTreeFormat(headId2, UUID.randomUUID()));

            var ex = assertThrows(ServiceException.class, () -> filler.fill(props(1), factoryItem, null));
            assertEquals(ErrorCodeTwins.FACTORY_INCORRECT.getCode(), ex.getErrorCode());
            verifyNoInteractions(twinService);
        }

        @Test
        void fill_depthBelowOne_throwsFactoryIncorrect() {
            var headId = UUID.randomUUID();
            var factoryItem = buildFactoryItem(LTreeUtils.convertToChainLTreeFormat(headId, UUID.randomUUID()));

            var ex = assertThrows(ServiceException.class, () -> filler.fill(props(0), factoryItem, null));
            assertEquals(ErrorCodeTwins.FACTORY_INCORRECT.getCode(), ex.getErrorCode());
            verifyNoInteractions(twinService);
        }
    }

    @Test
    void canBeOptional_returnsFalse() {
        assertFalse(filler.canBeOptional());
    }
}
