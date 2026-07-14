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
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.factory.filler.FillerForwardLinksFromContextTwin;
import org.twins.core.service.link.LinkService;
import org.twins.core.service.link.TwinLinkService;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

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

    private Properties props() {
        var p = new Properties();
        p.setProperty("linksIds", LINK_ID.toString());
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
                .setLink(link)
                .setLinkId(linkId)
                .setDstTwinId(dstTwinId)
                .setDstTwin(new TwinEntity().setId(dstTwinId));
    }

    @Nested
    class Fill {

        @Test
        void fill_linksFound_clonesForwardLinksToOutput() throws ServiceException {
            // NAME promises: copy the context twin's forward links (of the configured link ids) onto the output twin.
            var contextTwin = new TwinEntity().setId(UUID.randomUUID());
            var factoryItem = buildFactoryItem(contextTwin);
            when(twinLinkService.findTwinForwardLinks(eq(contextTwin), eq(Set.of(LINK_ID))))
                    .thenReturn(List.of(link(LINK_ID, DST_TWIN_ID)));

            filler.fill(props(), List.of(factoryItem), null, false);

            var create = (TwinCreate) factoryItem.getOutput();
            assertNotNull(create.getLinksEntityList());
            assertEquals(1, create.getLinksEntityList().size());
            var added = create.getLinksEntityList().get(0);
            assertEquals(LINK_ID, added.getLinkId());
            assertEquals(DST_TWIN_ID, added.getDstTwinId());
        }

        @Test
        void fill_noLinks_throwsStepError() throws ServiceException {
            var contextTwin = new TwinEntity().setId(UUID.randomUUID());
            var factoryItem = buildFactoryItem(contextTwin);
            when(twinLinkService.findTwinForwardLinks(eq(contextTwin), eq(Set.of(LINK_ID))))
                    .thenReturn(List.of());

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(), List.of(factoryItem), null, false));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
        }
    }
}
