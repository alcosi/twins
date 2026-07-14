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
import org.twins.core.featurer.factory.filler.FillerForwardLinkFromOutputTwinLinkDstTwinHead;
import org.twins.core.service.link.LinkService;
import org.twins.core.service.link.TwinLinkService;
import org.twins.core.service.twin.TwinService;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class FillerForwardLinkFromOutputTwinLinkDstTwinHeadTest extends BaseUnitTest {

    @Mock
    private TwinLinkService twinLinkService;

    @Mock
    private LinkService linkService;

    @Mock
    private TwinService twinService;

    private FillerForwardLinkFromOutputTwinLinkDstTwinHead filler;

    private static final UUID HEAD_FROM_LINK_ID = UUID.randomUUID();
    private static final UUID NEW_LINK_ID = UUID.randomUUID();
    private static final UUID DST_TWIN_ID = UUID.randomUUID();
    private static final UUID HEAD_TWIN_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() throws Exception {
        filler = new FillerForwardLinkFromOutputTwinLinkDstTwinHead();
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
        p.setProperty("headFromLink", HEAD_FROM_LINK_ID.toString());
        p.setProperty("newLinksId", NEW_LINK_ID.toString());
        return p;
    }

    private FactoryItem buildFactoryItem(TwinLinkEntity outputLink) {
        var outputTwinEntity = new TwinEntity().setId(UUID.randomUUID());
        var output = new TwinCreate();
        output.setTwinEntity(outputTwinEntity);
        if (outputLink != null)
            output.setLinksEntityList(new ArrayList<>(List.of(outputLink)));
        return new FactoryItem().setOutput(output);
    }

    @Nested
    class Fill {

        @Test
        void fill_singleMatchingLink_createsNewLinkToHeadOfDst() throws ServiceException {
            // NAME promises: in the OUTPUT twin's link list, find the head-from link; take its dst twin; take that dst's head;
            //                create a new link of `newLinksId` type on the output pointing to that head.
            var dstTwin = new TwinEntity().setId(DST_TWIN_ID);
            var outputLink = new TwinLinkEntity()
                    .setLinkId(HEAD_FROM_LINK_ID)
                    .setDstTwin(dstTwin)
                    .setDstTwinId(DST_TWIN_ID);
            var factoryItem = buildFactoryItem(outputLink);
            var headTwin = new TwinEntity().setId(HEAD_TWIN_ID);
            when(twinService.loadHead(dstTwin)).thenReturn(headTwin);
            var newLinkEntity = new LinkEntity().setId(NEW_LINK_ID);
            when(linkService.findEntitySafe(NEW_LINK_ID)).thenReturn(newLinkEntity);

            filler.fill(props(), List.of(factoryItem), null, false);

            var create = (TwinCreate) factoryItem.getOutput();
            assertNotNull(create.getLinksEntityList());
            // original link + new link
            assertEquals(2, create.getLinksEntityList().size());
            var added = create.getLinksEntityList().get(1);
            assertEquals(NEW_LINK_ID, added.getLinkId());
            assertEquals(HEAD_TWIN_ID, added.getDstTwinId());
        }

        @Test
        void fill_noLinksOnOutput_throwsStepError() {
            var factoryItem = buildFactoryItem(null);

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(), List.of(factoryItem), null, false));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
        }

        @Test
        void fill_noMatchingLink_throwsStepError() {
            var otherLink = new TwinLinkEntity().setLinkId(UUID.randomUUID());
            var factoryItem = buildFactoryItem(otherLink);

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(), List.of(factoryItem), null, false));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
        }

        @Test
        void fill_multipleMatchingLinks_throwsStepError() {
            var dst1 = new TwinEntity().setId(UUID.randomUUID());
            var dst2 = new TwinEntity().setId(UUID.randomUUID());
            var output = new TwinCreate();
            output.setTwinEntity(new TwinEntity().setId(UUID.randomUUID()));
            output.setLinksEntityList(new ArrayList<>(List.of(
                    new TwinLinkEntity().setLinkId(HEAD_FROM_LINK_ID).setDstTwin(dst1).setDstTwinId(dst1.getId()),
                    new TwinLinkEntity().setLinkId(HEAD_FROM_LINK_ID).setDstTwin(dst2).setDstTwinId(dst2.getId())
            )));
            var factoryItem = new FactoryItem().setOutput(output);

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(props(), List.of(factoryItem), null, false));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
        }
    }
}
