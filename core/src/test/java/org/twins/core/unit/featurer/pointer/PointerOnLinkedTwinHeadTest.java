package org.twins.core.featurer.pointer;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.service.link.TwinLinkService;
import org.twins.core.service.twin.TwinService;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.doAnswer;

class PointerOnLinkedTwinHeadTest extends BaseUnitTest {

    @Mock
    private TwinLinkService twinLinkService;
    @Mock
    private TwinService twinService;

    private PointerOnLinkedTwinHead pointer;
    private UUID linkIdValue;

    @BeforeEach
    void setUp() {
        pointer = new PointerOnLinkedTwinHead(twinLinkService, twinService);
        linkIdValue = UUID.randomUUID();
    }

    private Properties props() {
        var p = new Properties();
        p.put("linkId", linkIdValue.toString());
        return p;
    }

    private TwinLinkEntity link(UUID linkId, TwinEntity src, TwinEntity dst) {
        var l = new TwinLinkEntity();
        l.setId(UUID.randomUUID());
        l.setLinkId(linkId);
        l.setSrcTwinId(src.getId());
        l.setDstTwin(dst);
        return l;
    }

    private void setupLinks(TwinLinkEntity... links) throws ServiceException {
        doAnswer(invocation -> {
            Collection<TwinEntity> twins = invocation.getArgument(0);
            for (TwinEntity t : twins) {
                var result = new TwinLinkService.FindTwinLinksResult();
                for (var l : links) {
                    result.getForwardLinks().add(l);
                }
                t.setTwinLinks(result);
            }
            return null;
        }).when(twinLinkService).loadTwinLinks(anyCollection());
    }

    private void setupHeads(Map<TwinEntity, TwinEntity> headOf) throws ServiceException {
        doAnswer(invocation -> {
            Collection<TwinEntity> twins = invocation.getArgument(0);
            for (TwinEntity t : twins) {
                TwinEntity head = headOf.get(t);
                if (head != null) {
                    t.setHeadTwin(head);
                }
            }
            return null;
        }).when(twinService).loadHead(anyCollection());
    }

    @Test
    void load_linkedTwinHasHead_returnsHead() throws ServiceException {
        var head = new TwinEntity();
        var linked = new TwinEntity().setId(UUID.randomUUID());
        var src = new TwinEntity().setId(UUID.randomUUID());
        setupLinks(link(linkIdValue, src, linked));
        setupHeads(Map.of(linked, head));

        Map<UUID, TwinEntity> result = pointer.load(props(), java.util.List.of(src));

        assertSame(head, result.get(src.getId()));
    }

    @Test
    void load_noForwardLink_resolvesToNull() throws ServiceException {
        var src = new TwinEntity().setId(UUID.randomUUID());
        setupLinks();

        Map<UUID, TwinEntity> result = pointer.load(props(), java.util.List.of(src));

        assertNull(result.get(src.getId()));
    }

    @Test
    void load_linkedTwinHasNoHead_resolvesToNull() throws ServiceException {
        var linked = new TwinEntity().setId(UUID.randomUUID());
        var src = new TwinEntity().setId(UUID.randomUUID());
        setupLinks(link(linkIdValue, src, linked));
        setupHeads(Map.of());

        Map<UUID, TwinEntity> result = pointer.load(props(), java.util.List.of(src));

        assertNull(result.get(src.getId()));
    }
}
