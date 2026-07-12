package org.twins.core.featurer.pointer;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.link.TwinLinkService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.doAnswer;

class PointerOnLinkedTwinTest extends BaseUnitTest {

    @Mock
    private TwinLinkService twinLinkService;

    private PointerOnLinkedTwin pointer;

    private UUID linkIdValue;

    @BeforeEach
    void setUp() {
        pointer = new PointerOnLinkedTwin(twinLinkService);
        linkIdValue = UUID.randomUUID();
    }

    private Properties props() {
        var props = new Properties();
        props.put("linkId", linkIdValue.toString());
        return props;
    }

    private TwinLinkEntity linkEntity(UUID linkId, TwinEntity srcTwin, TwinEntity dstTwin) {
        var link = new TwinLinkEntity();
        link.setId(UUID.randomUUID());
        link.setLinkId(linkId);
        link.setSrcTwinId(srcTwin.getId());
        link.setDstTwin(dstTwin);
        return link;
    }

    private void setupTwinLinks(TwinEntity srcTwin, TwinLinkEntity... links) throws ServiceException {
        // load() now calls the batch overload loadTwinLinks(Collection); populate every twin with the links.
        doAnswer(invocation -> {
            Collection<TwinEntity> twins = invocation.getArgument(0);
            for (TwinEntity entity : twins) {
                var result = new TwinLinkService.FindTwinLinksResult();
                for (var link : links) {
                    result.getForwardLinks().add(link);
                }
                entity.setTwinLinks(result);
            }
            return null;
        }).when(twinLinkService).loadTwinLinks(anyCollection());
    }

    @Nested
    class Load {

        @Test
        void load_noForwardLinks_returnsEmptyMapping() throws ServiceException {
            var srcTwin = new TwinEntity().setId(UUID.randomUUID());
            setupTwinLinks(srcTwin);

            Map<UUID, TwinEntity> result = pointer.load(props(), List.of(srcTwin));

            assertNull(result.get(srcTwin.getId()));
        }

        @Test
        void load_singleForwardLink_returnsDstTwin() throws ServiceException {
            var dstTwin = new TwinEntity();
            var srcTwin = new TwinEntity().setId(UUID.randomUUID());
            setupTwinLinks(srcTwin, linkEntity(linkIdValue, srcTwin, dstTwin));

            Map<UUID, TwinEntity> result = pointer.load(props(), List.of(srcTwin));

            assertSame(dstTwin, result.get(srcTwin.getId()));
        }

        @Test
        void load_multipleForwardLinks_throwsPointerNonSingle() throws ServiceException {
            var srcTwin = new TwinEntity().setId(UUID.randomUUID());
            setupTwinLinks(
                    srcTwin,
                    linkEntity(linkIdValue, srcTwin, new TwinEntity()),
                    linkEntity(linkIdValue, srcTwin, new TwinEntity())
            );

            var ex = assertThrows(
                    ServiceException.class,
                    () -> pointer.load(props(), List.of(srcTwin))
            );
            assertEquals(ErrorCodeTwins.POINTER_NON_SINGLE.getCode(), ex.getErrorCode());
        }

        @Test
        void load_linksOfDifferentType_notReturned() throws ServiceException {
            var srcTwin = new TwinEntity().setId(UUID.randomUUID());
            setupTwinLinks(srcTwin, linkEntity(UUID.randomUUID(), srcTwin, new TwinEntity()));

            Map<UUID, TwinEntity> result = pointer.load(props(), List.of(srcTwin));

            assertNull(result.get(srcTwin.getId()));
        }
    }
}
