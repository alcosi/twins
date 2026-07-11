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
import org.twins.core.enums.link.LinkType;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.factory.filler.FillerBackwardLinksFromContextTwinAll;
import org.twins.core.service.link.LinkService;
import org.twins.core.service.link.TwinLinkService;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FillerBackwardLinksFromContextTwinAllTest extends BaseUnitTest {

    @Mock
    private TwinLinkService twinLinkService;

    @Mock
    private LinkService linkService;

    private FillerBackwardLinksFromContextTwinAll filler;

    private static final UUID CONTEXT_TWIN_ID = UUID.randomUUID();
    private static final UUID SRC_TWIN_ID = UUID.randomUUID();
    private static final UUID LINK_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() throws Exception {
        filler = new FillerBackwardLinksFromContextTwinAll();
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

    private Properties props(boolean uniq) {
        var p = new Properties();
        p.setProperty("uniqForSrcRelink", Boolean.toString(uniq));
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

    private TwinLinkEntity backwardLink(UUID linkId, UUID srcTwinId, LinkType linkType) {
        var link = new LinkEntity().setId(linkId).setType(linkType);
        return new TwinLinkEntity()
                .setLink(link)
                .setLinkId(linkId)
                .setSrcTwinId(srcTwinId)
                .setSrcTwin(new TwinEntity().setId(srcTwinId));
    }

    @Nested
    class Fill {

        @Test
        void fill_manyToManyLink_alwaysCopiedToOutput() throws ServiceException {
            // NAME promises: copy context-twin's backward links onto output. ManyToMany links are always relinked.
            var contextTwin = new TwinEntity().setId(CONTEXT_TWIN_ID);
            var factoryItem = buildFactoryItem(contextTwin);
            when(twinLinkService.findTwinBackwardLinks(CONTEXT_TWIN_ID))
                    .thenReturn(List.of(backwardLink(LINK_ID, SRC_TWIN_ID, LinkType.ManyToMany)));

            // uniq=false; should still be added because the link is ManyToMany
            filler.fill(props(false), factoryItem, null);

            var create = (TwinCreate) factoryItem.getOutput();
            assertNotNull(create.getLinksEntityList());
            assertEquals(1, create.getLinksEntityList().size());
            var added = create.getLinksEntityList().get(0);
            // backward relink: dst is the original src twin
            assertEquals(SRC_TWIN_ID, added.getDstTwinId());
            assertEquals(LINK_ID, added.getLinkId());
        }

        @Test
        void fill_oneToManyLink_uniqFalse_isSkipped() throws ServiceException {
            // X-to-One (ManyToOne here) with uniq=false -> not relinked (skipped).
            var contextTwin = new TwinEntity().setId(CONTEXT_TWIN_ID);
            var factoryItem = buildFactoryItem(contextTwin);
            when(twinLinkService.findTwinBackwardLinks(CONTEXT_TWIN_ID))
                    .thenReturn(List.of(backwardLink(LINK_ID, SRC_TWIN_ID, LinkType.ManyToOne)));

            filler.fill(props(false), factoryItem, null);

            var create = (TwinCreate) factoryItem.getOutput();
            assertTrue(create.getLinksEntityList() == null || create.getLinksEntityList().isEmpty());
        }

        @Test
        void fill_oneToManyLink_uniqTrue_isCopied() throws ServiceException {
            // X-to-One with uniq=true -> force relink.
            var contextTwin = new TwinEntity().setId(CONTEXT_TWIN_ID);
            var factoryItem = buildFactoryItem(contextTwin);
            when(twinLinkService.findTwinBackwardLinks(CONTEXT_TWIN_ID))
                    .thenReturn(List.of(backwardLink(LINK_ID, SRC_TWIN_ID, LinkType.ManyToOne)));

            filler.fill(props(true), factoryItem, null);

            var create = (TwinCreate) factoryItem.getOutput();
            assertNotNull(create.getLinksEntityList());
            assertEquals(1, create.getLinksEntityList().size());
        }

        @Test
        void fill_noBackwardLinks_throwsStepError() throws ServiceException {
            var contextTwin = new TwinEntity().setId(CONTEXT_TWIN_ID);
            var factoryItem = buildFactoryItem(contextTwin);
            when(twinLinkService.findTwinBackwardLinks(CONTEXT_TWIN_ID)).thenReturn(List.of());

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(false), factoryItem, null));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
        }
    }
}
