package org.twins.core.unit.featurer.factory.filler;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.KitGrouped;
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
import org.twins.core.featurer.factory.filler.FillerForwardLinksFromContextTwinAll;
import org.twins.core.service.link.LinkService;
import org.twins.core.service.link.TwinLinkService;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FillerForwardLinksFromContextTwinAllTest extends BaseUnitTest {

    @Mock
    private TwinLinkService twinLinkService;

    @Mock
    private LinkService linkService;

    private FillerForwardLinksFromContextTwinAll filler;

    private static final UUID LINK_ID = UUID.randomUUID();
    private static final UUID DST_TWIN_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() throws Exception {
        filler = new FillerForwardLinksFromContextTwinAll();
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

    private KitGrouped<TwinLinkEntity, UUID, UUID> kitOf(TwinLinkEntity... items) {
        return new KitGrouped<>(
                List.of(items),
                TwinLinkEntity::getLinkId,
                TwinLinkEntity::getLinkId);
    }

    @Nested
    class Fill {

        @Test
        void fill_linksFound_clonesAllForwardLinksToOutput() throws ServiceException {
            // NAME promises: copy ALL context-twin forward links onto the output twin.
            var contextTwin = new TwinEntity().setId(UUID.randomUUID());
            var factoryItem = buildFactoryItem(contextTwin);
            when(twinLinkService.findTwinForwardLinks(contextTwin))
                    .thenReturn(kitOf(link(LINK_ID, DST_TWIN_ID)));

            filler.fill(new Properties(), factoryItem, null);

            var create = (TwinCreate) factoryItem.getOutput();
            assertNotNull(create.getLinksEntityList());
            assertEquals(1, create.getLinksEntityList().size());
            assertEquals(LINK_ID, create.getLinksEntityList().get(0).getLinkId());
        }

        @Test
        void fill_noLinks_throwsStepError() throws ServiceException {
            var contextTwin = new TwinEntity().setId(UUID.randomUUID());
            var factoryItem = buildFactoryItem(contextTwin);
            when(twinLinkService.findTwinForwardLinks(contextTwin))
                    .thenReturn(new KitGrouped<>(List.of(), TwinLinkEntity::getLinkId, TwinLinkEntity::getLinkId));

            var ex = assertThrows(ServiceException.class,
                    () -> filler.fill(new Properties(), factoryItem, null));
            assertEquals(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR.getCode(), ex.getErrorCode());
        }

        @Test
        void fill_updateOutput_recordsLinksInTwinLinkCud() throws ServiceException {
            // Exercises the abstract FillerLinks.addLinks(TwinUpdate) branch: links go to twinLinkCUD.createList.
            var contextTwin = new TwinEntity().setId(UUID.randomUUID());
            var dbTwin = new org.twins.core.dao.twin.TwinEntity()
                    .setTwinStatus(new org.twins.core.dao.twin.TwinStatusEntity().setType(org.twins.core.enums.status.StatusType.BASIC));
            var update = new org.twins.core.domain.twinoperation.TwinUpdate();
            update.setDbTwinEntity(dbTwin).setTwinEntity(new org.twins.core.dao.twin.TwinEntity());
            var contextOutput = new TwinCreate();
            contextOutput.setTwinEntity(contextTwin);
            var contextItem = new FactoryItem().setOutput(contextOutput);
            var factoryItem = new FactoryItem().setOutput(update).setContextFactoryItemList(List.of(contextItem));
            when(twinLinkService.findTwinForwardLinks(contextTwin))
                    .thenReturn(kitOf(link(LINK_ID, DST_TWIN_ID)));

            filler.fill(new Properties(), factoryItem, null);

            assertNotNull(update.getTwinLinkCUD());
            assertNotNull(update.getTwinLinkCUD().getCreateList());
            assertEquals(1, update.getTwinLinkCUD().getCreateList().size());
            assertEquals(LINK_ID, update.getTwinLinkCUD().getCreateList().get(0).getLinkId());
        }
    }
}
