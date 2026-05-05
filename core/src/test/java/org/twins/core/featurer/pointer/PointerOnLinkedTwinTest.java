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

import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
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

    private TwinLinkEntity linkEntity(UUID linkId, TwinEntity dstTwin) {
        var link = new TwinLinkEntity();
        link.setId(UUID.randomUUID());
        link.setLinkId(linkId);
        link.setDstTwin(dstTwin);
        return link;
    }

    private void setupTwinLinks(TwinEntity srcTwin, TwinLinkEntity... links) throws ServiceException {
        doAnswer(invocation -> {
            TwinEntity entity = invocation.getArgument(0);
            var result = new TwinLinkService.FindTwinLinksResult();
            for (var link : links)
                result.getForwardLinks().add(link);
            entity.setTwinLinks(result);
            return null;
        }).when(twinLinkService).loadTwinLinks(srcTwin);
    }

    @Nested
    class Point {

        @Test
        void point_noForwardLinks_returnsNull() throws ServiceException {
            var srcTwin = new TwinEntity();
            setupTwinLinks(srcTwin);

            var result = pointer.point(props(), srcTwin);

            assertNull(result);
        }

        @Test
        void point_singleForwardLink_returnsDstTwin() throws ServiceException {
            var dstTwin = new TwinEntity();
            var srcTwin = new TwinEntity();
            setupTwinLinks(srcTwin, linkEntity(linkIdValue, dstTwin));

            var result = pointer.point(props(), srcTwin);

            assertSame(dstTwin, result);
        }

        @Test
        void point_multipleForwardLinks_throwsPointerNonSingle() throws ServiceException {
            var srcTwin = new TwinEntity();
            setupTwinLinks(
                    srcTwin,
                    linkEntity(linkIdValue, new TwinEntity()),
                    linkEntity(linkIdValue, new TwinEntity())
            );

            var ex = assertThrows(
                    ServiceException.class,
                    () -> pointer.point(props(), srcTwin)
            );
            assertEquals(ErrorCodeTwins.POINTER_NON_SINGLE.getCode(), ex.getErrorCode());
        }

        @Test
        void point_linksOfDifferentType_notReturned() throws ServiceException {
            var srcTwin = new TwinEntity();
            setupTwinLinks(srcTwin, linkEntity(UUID.randomUUID(), new TwinEntity()));

            var result = pointer.point(props(), srcTwin);

            assertNull(result);
        }
    }
}
