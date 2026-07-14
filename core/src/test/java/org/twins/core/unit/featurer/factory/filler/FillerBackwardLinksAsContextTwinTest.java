package org.twins.core.unit.featurer.factory.filler;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.factory.filler.FillerBackwardLinksAsContextTwin;
import org.twins.core.service.link.LinkService;
import org.twins.core.service.link.TwinLinkService;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class FillerBackwardLinksAsContextTwinTest extends BaseUnitTest {

    @Mock
    private TwinLinkService twinLinkService;

    @Mock
    private LinkService linkService;

    private FillerBackwardLinksAsContextTwin filler;

    private static final UUID CONTEXT_TWIN_ID = UUID.randomUUID();
    private static final UUID LINK_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() throws Exception {
        filler = new FillerBackwardLinksAsContextTwin();
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

    private FactoryItem buildFactoryItem(TwinClassEntity contextClass, TwinClassEntity outputClass, TwinEntity contextTwin) {
        var output = new TwinCreate();
        output.setTwinEntity(new TwinEntity().setTwinClass(outputClass));
        var contextOutput = new TwinCreate();
        contextOutput.setTwinEntity(contextTwin);
        var contextItem = new FactoryItem().setOutput(contextOutput);
        return new FactoryItem().setOutput(output).setContextFactoryItemList(List.of(contextItem));
    }

    @Nested
    class Fill {

        @Test
        void fill_linkFound_addsBackwardLinkFromOutputToContext() throws ServiceException {
            // NAME promises: for each context twin, create a backward link on the output pointing to that context twin.
            var contextClass = new TwinClassEntity().setId(UUID.randomUUID());
            var outputClass = new TwinClassEntity().setId(UUID.randomUUID());
            var contextTwin = new TwinEntity().setId(CONTEXT_TWIN_ID).setTwinClass(contextClass);
            var factoryItem = buildFactoryItem(contextClass, outputClass, contextTwin);
            var link = new LinkEntity().setId(LINK_ID);
            when(linkService.findLinks(contextClass, outputClass)).thenReturn(List.of(link));

            filler.fill(props(true), List.of(factoryItem), null, false);

            var create = (TwinCreate) factoryItem.getOutput();
            assertNotNull(create.getLinksEntityList());
            assertEquals(1, create.getLinksEntityList().size());
            var added = create.getLinksEntityList().get(0);
            // backward link: dst is the context twin
            assertEquals(CONTEXT_TWIN_ID, added.getDstTwinId());
            assertEquals(LINK_ID, added.getLinkId());
            assertTrue(added.isUniqForSrcRelink());
        }

        @Test
        void fill_noLinks_throwsStepError() {
            var contextClass = new TwinClassEntity().setId(UUID.randomUUID());
            var outputClass = new TwinClassEntity().setId(UUID.randomUUID());
            var contextTwin = new TwinEntity().setId(CONTEXT_TWIN_ID).setTwinClass(contextClass);
            var factoryItem = buildFactoryItem(contextClass, outputClass, contextTwin);
            when(linkService.findLinks(contextClass, outputClass)).thenReturn(List.of());

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(false), List.of(factoryItem), null, false));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
        }
    }
}
