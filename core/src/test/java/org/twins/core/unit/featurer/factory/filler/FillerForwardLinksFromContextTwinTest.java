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
import org.twins.core.domain.twinoperation.TwinUpdate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.factory.filler.FillerForwardLinksFromContextTwin;
import org.twins.core.service.link.LinkService;
import org.twins.core.service.twinlink.TwinLinkService;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.doAnswer;

class FillerForwardLinksFromContextTwinTest extends BaseUnitTest {

    @Mock
    private TwinLinkService twinLinkService;

    @Mock
    private LinkService linkService;

    private FillerForwardLinksFromContextTwin filler;

    private static final UUID LINK_ID = UUID.randomUUID();
    private static final UUID DST_TWIN_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() throws Exception {
        filler = new FillerForwardLinksFromContextTwin();
        inject(filler, "twinLinkService", twinLinkService);
        inject(filler, "linkService", linkService);
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

    private Properties props(String linksIdsCsv) {
        var p = new Properties();
        if (linksIdsCsv != null)
            p.setProperty("linksIds", linksIdsCsv);
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

    private TwinLinkEntity link(UUID linkId, UUID dstTwinId) {
        var link = new LinkEntity().setId(linkId);
        return new TwinLinkEntity()
                .setId(UUID.randomUUID()) // Kit keys links by their own id and rejects null keys
                .setLink(link)
                .setLinkId(linkId)
                .setDstTwinId(dstTwinId)
                .setDstTwin(new TwinEntity().setId(dstTwinId));
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

    @Nested
    class Fill {

        @Test
        void fill_linksIdsSet_clonesOnlyMatchingForwardLinks() throws ServiceException {
            // NAME promises: copy the context twin's forward links (of the configured link ids) onto the output twin.
            var contextTwin = new TwinEntity().setId(UUID.randomUUID());
            var factoryItem = buildFactoryItem(contextTwin);
            plantForwardLinks(link(LINK_ID, DST_TWIN_ID), link(UUID.randomUUID(), UUID.randomUUID()));

            filler.fill(props(LINK_ID.toString()), new FactoryItemsBatch().add(factoryItem), null, false);

            var create = (TwinCreate) factoryItem.getOutput();
            assertNotNull(create.getLinksEntityList());
            // only the link matching linksIds is cloned
            assertEquals(1, create.getLinksEntityList().size());
            var added = create.getLinksEntityList().get(0);
            assertEquals(LINK_ID, added.getLinkId());
            assertEquals(DST_TWIN_ID, added.getDstTwinId());
        }

        @Test
        void fill_linksIdsEmpty_clonesAllForwardLinks() throws ServiceException {
            // the former FillerForwardLinksFromContextTwinAll behavior: no linksIds -> no filtering
            var contextTwin = new TwinEntity().setId(UUID.randomUUID());
            var factoryItem = buildFactoryItem(contextTwin);
            plantForwardLinks(link(LINK_ID, DST_TWIN_ID), link(UUID.randomUUID(), UUID.randomUUID()));

            filler.fill(props(null), new FactoryItemsBatch().add(factoryItem), null, false);

            var create = (TwinCreate) factoryItem.getOutput();
            assertNotNull(create.getLinksEntityList());
            assertEquals(2, create.getLinksEntityList().size());
        }

        @Test
        void fill_noMatchingLinks_throwsStepError() throws ServiceException {
            var contextTwin = new TwinEntity().setId(UUID.randomUUID());
            var factoryItem = buildFactoryItem(contextTwin);
            plantForwardLinks(link(UUID.randomUUID(), UUID.randomUUID())); // different link id

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(LINK_ID.toString()), new FactoryItemsBatch().add(factoryItem), null, false));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
        }

        @Test
        void fill_noLinksAtAll_throwsStepError() throws ServiceException {
            var contextTwin = new TwinEntity().setId(UUID.randomUUID());
            var factoryItem = buildFactoryItem(contextTwin);
            plantForwardLinks(); // loaded, but no forward links

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(null), new FactoryItemsBatch().add(factoryItem), null, false));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
        }

        @Test
        void fill_updateOutput_recordsLinksInTwinLinkCud() throws ServiceException {
            // Exercises the FillerLinks.addLinks(TwinUpdate) branch: links go to twinLinkCUD.createList.
            var contextTwin = new TwinEntity().setId(UUID.randomUUID());
            var dbTwin = new org.twins.core.dao.twin.TwinEntity()
                    .setTwinStatus(new org.twins.core.dao.twin.TwinStatusEntity().setType(org.twins.core.enums.status.StatusType.BASIC));
            var update = new TwinUpdate();
            update.setDbTwinEntity(dbTwin).setTwinEntity(new org.twins.core.dao.twin.TwinEntity());
            var contextOutput = new TwinCreate();
            contextOutput.setTwinEntity(contextTwin);
            var contextItem = new FactoryItem().setOutput(contextOutput);
            var factoryItem = new FactoryItem().setOutput(update).setContextFactoryItemList(List.of(contextItem));
            plantForwardLinks(link(LINK_ID, DST_TWIN_ID));

            filler.fill(props(LINK_ID.toString()), new FactoryItemsBatch().add(factoryItem), null, false);

            assertNotNull(update.getTwinLinkCUD());
            assertNotNull(update.getTwinLinkCUD().getCreateList());
            assertEquals(1, update.getTwinLinkCUD().getCreateList().size());
            assertEquals(LINK_ID, update.getTwinLinkCUD().getCreateList().get(0).getTwinLink().getLinkId());
        }
    }
}
