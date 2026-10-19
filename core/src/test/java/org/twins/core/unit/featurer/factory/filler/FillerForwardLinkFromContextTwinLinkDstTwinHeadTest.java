package org.twins.core.unit.featurer.factory.filler;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.factory.FactoryItemsBatch;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.factory.filler.FillerForwardLinkFromContextTwinLinkDstTwinHead;
import org.twins.core.service.link.LinkService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinlink.TwinLinkService;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

class FillerForwardLinkFromContextTwinLinkDstTwinHeadTest extends BaseUnitTest {

    @Mock
    private TwinLinkService twinLinkService;

    @Mock
    private LinkService linkService;

    @Mock
    private TwinService twinService;

    private FillerForwardLinkFromContextTwinLinkDstTwinHead filler;

    private static final UUID HEAD_HUNTER_LINK_ID = UUID.randomUUID();
    private static final UUID NEW_LINK_ID = UUID.randomUUID();
    private static final UUID DST_TWIN_ID = UUID.randomUUID();
    private static final UUID HEAD_TWIN_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() throws Exception {
        filler = new FillerForwardLinkFromContextTwinLinkDstTwinHead();
        inject(filler, "twinLinkService", twinLinkService);
        inject(filler, "linkService", linkService);
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

    private Properties props() {
        var p = new Properties();
        p.setProperty("headHunterLink", HEAD_HUNTER_LINK_ID.toString());
        p.setProperty("newLinksId", NEW_LINK_ID.toString());
        return p;
    }

    private FactoryItem buildFactoryItem(TwinEntity contextTwin) {
        var output = new TwinCreate();
        output.setTwinEntity(new TwinEntity());
        var contextOutput = new TwinCreate();
        contextOutput.setTwinEntity(contextTwin);
        var contextItem = new FactoryItem().setOutput(contextOutput);
        return new FactoryItem().setOutput(output).setContextFactoryItemList(List.of(contextItem));
    }

    /** Item whose CONTEXT item itself has a context twin (one level up the walk). */
    private FactoryItem buildFactoryItem(TwinEntity contextTwin, TwinEntity grandContextTwin) {
        var grandOutput = new TwinCreate();
        grandOutput.setTwinEntity(grandContextTwin);
        var grandContextItem = new FactoryItem().setOutput(grandOutput);
        var contextOutput = new TwinCreate();
        contextOutput.setTwinEntity(contextTwin);
        var contextItem = new FactoryItem().setOutput(contextOutput).setContextFactoryItemList(List.of(grandContextItem));
        var output = new TwinCreate();
        output.setTwinEntity(new TwinEntity());
        return new FactoryItem().setOutput(output).setContextFactoryItemList(List.of(contextItem));
    }

    private TwinLinkEntity link(UUID linkId, TwinEntity dstTwin) {
        return new TwinLinkEntity()
                .setId(UUID.randomUUID()) // Kit keys links by their own id and rejects null keys
                .setLinkId(linkId)
                .setDstTwin(dstTwin)
                .setDstTwinId(dstTwin.getId());
    }

    /** Simulates the bulk load the real TwinLinkService performs: marks twins as loaded and fills their forward links. */
    @SuppressWarnings("unchecked")
    private void plantForwardLinks(TwinLinkEntity... links) throws ServiceException {
        doAnswer(inv -> {
            for (TwinEntity twin : (Collection<TwinEntity>) inv.getArgument(0)) {
                var result = new TwinLinkService.FindTwinLinksResult().setTwinId(twin.getId());
                for (TwinLinkEntity link : links)
                    result.getForwardLinks().add(link);
                twin.setTwinLinks(result);
            }
            return null;
        }).when(twinLinkService).loadTwinLinks(anyCollection());
    }

    /** Variant for the hierarchy walk: only the target twin gets links, every other loaded twin stays empty. */
    @SuppressWarnings("unchecked")
    private void plantForwardLinksOn(TwinEntity target, TwinLinkEntity... links) throws ServiceException {
        doAnswer(inv -> {
            for (TwinEntity twin : (Collection<TwinEntity>) inv.getArgument(0)) {
                var result = new TwinLinkService.FindTwinLinksResult().setTwinId(twin.getId());
                if (twin == target)
                    for (TwinLinkEntity link : links)
                        result.getForwardLinks().add(link);
                twin.setTwinLinks(result);
            }
            return null;
        }).when(twinLinkService).loadTwinLinks(anyCollection());
    }

    @Nested
    class Fill {

        @Test
        void fill_singleMatchingLink_createsNewLinkToHeadOfDst() throws ServiceException {
            // NAME promises: in the context twin, find the head-hunter link; take its dst twin; take that dst's head;
            //                create a new link of `newLinksId` type on the output pointing to that head.
            var contextTwin = new TwinEntity().setId(UUID.randomUUID());
            var factoryItem = buildFactoryItem(contextTwin);
            var dstTwin = new TwinEntity().setId(DST_TWIN_ID);
            // loadHead mutates dstTwin.headTwin in prod; the mock is a no-op, so seed the field
            // that the distribution reads back via dstTwin.getHeadTwin().
            var headTwin = new TwinEntity().setId(HEAD_TWIN_ID);
            dstTwin.setHeadTwin(headTwin);
            plantForwardLinks(link(HEAD_HUNTER_LINK_ID, dstTwin));
            var newLinkEntity = new LinkEntity().setId(NEW_LINK_ID);
            when(linkService.findEntitySafe(NEW_LINK_ID)).thenReturn(newLinkEntity);

            filler.fill(props(), new FactoryItemsBatch().add(factoryItem), null, false);

            var create = (TwinCreate) factoryItem.getOutput();
            assertNotNull(create.getLinksEntityList());
            assertEquals(1, create.getLinksEntityList().size());
            var added = create.getLinksEntityList().get(0);
            assertEquals(NEW_LINK_ID, added.getLinkId());
            assertEquals(HEAD_TWIN_ID, added.getDstTwinId());
        }

        @Test
        void fill_linkNotFoundAtLevel0_foundOneLevelUp_theWalkDescends() throws ServiceException {
            // the old lookupLink recursion: level 0 has no matching link -> walk up the context chain
            var contextTwin = new TwinEntity().setId(UUID.randomUUID()); // level 0 — no links
            var grandContextTwin = new TwinEntity().setId(UUID.randomUUID()); // level 1 — has the link
            var factoryItem = buildFactoryItem(contextTwin, grandContextTwin);
            var dstTwin = new TwinEntity().setId(DST_TWIN_ID);
            dstTwin.setHeadTwin(new TwinEntity().setId(HEAD_TWIN_ID));
            plantForwardLinksOn(grandContextTwin, link(HEAD_HUNTER_LINK_ID, dstTwin));
            when(linkService.findEntitySafe(NEW_LINK_ID)).thenReturn(new LinkEntity().setId(NEW_LINK_ID));

            filler.fill(props(), new FactoryItemsBatch().add(factoryItem), null, false);

            var create = (TwinCreate) factoryItem.getOutput();
            assertNotNull(create.getLinksEntityList());
            assertEquals(1, create.getLinksEntityList().size());
            assertEquals(HEAD_TWIN_ID, create.getLinksEntityList().get(0).getDstTwinId());
        }

        @Test
        void fill_noMatchingLinks_throwsStepError() throws ServiceException {
            var contextTwin = new TwinEntity().setId(UUID.randomUUID());
            var factoryItem = buildFactoryItem(contextTwin);
            plantForwardLinks(); // loaded, but no forward links; the walk cannot descend further
            // (the context item has no own context -> checkSingleContextItem fails the item)

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(), new FactoryItemsBatch().add(factoryItem), null, false));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
        }

        @Test
        void fill_multipleMatchingLinks_throwsStepError() throws ServiceException {
            var contextTwin = new TwinEntity().setId(UUID.randomUUID());
            var factoryItem = buildFactoryItem(contextTwin);
            var dst1 = new TwinEntity().setId(UUID.randomUUID());
            var dst2 = new TwinEntity().setId(UUID.randomUUID());
            plantForwardLinks(link(HEAD_HUNTER_LINK_ID, dst1), link(HEAD_HUNTER_LINK_ID, dst2));

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(), new FactoryItemsBatch().add(factoryItem), null, false));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
        }
    }
}
