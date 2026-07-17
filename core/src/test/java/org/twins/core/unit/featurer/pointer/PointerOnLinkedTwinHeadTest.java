package org.twins.core.featurer.pointer;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.Featurer;
import org.cambium.featurer.FeaturerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.dao.twin.TwinPointerEntity;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.link.TwinLinkService;
import org.twins.core.service.twin.TwinService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

class PointerOnLinkedTwinHeadTest extends BaseUnitTest {

    @Mock
    private TwinLinkService twinLinkService;
    @Mock
    private TwinService twinService;
    @Mock
    private FeaturerService featurerService;

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

    // ----------------------------------------------------------------------------------------------
    // Batch path (N publishers, one subclass lookup) — covers the shared collection helpers
    // (identity / followSingleForwardLink / toHead) on multi-element input, which the single-element
    // tests above do not exercise.
    // ----------------------------------------------------------------------------------------------

    @Test
    void load_batchMultipleSources_resolvesEachIndependently() throws ServiceException {
        var src1 = new TwinEntity().setId(UUID.randomUUID());
        var src2 = new TwinEntity().setId(UUID.randomUUID());
        var linked1 = new TwinEntity().setId(UUID.randomUUID());
        var linked2 = new TwinEntity().setId(UUID.randomUUID());
        var head1 = new TwinEntity();
        var head2 = new TwinEntity();
        doAnswer(invocation -> {
            Collection<TwinEntity> twins = invocation.getArgument(0);
            for (TwinEntity t : twins) {
                var result = new TwinLinkService.FindTwinLinksResult();
                if (t == src1) result.getForwardLinks().add(link(linkIdValue, src1, linked1));
                if (t == src2) result.getForwardLinks().add(link(linkIdValue, src2, linked2));
                t.setTwinLinks(result);
            }
            return null;
        }).when(twinLinkService).loadTwinLinks(anyCollection());
        doAnswer(invocation -> {
            Collection<TwinEntity> twins = invocation.getArgument(0);
            for (TwinEntity t : twins) {
                if (t == linked1) t.setHeadTwin(head1);
                if (t == linked2) t.setHeadTwin(head2);
            }
            return null;
        }).when(twinService).loadHead(anyCollection());

        Map<UUID, TwinEntity> result = pointer.load(props(), List.of(src1, src2));

        assertSame(head1, result.get(src1.getId()));
        assertSame(head2, result.get(src2.getId()));
    }

    @Test
    void load_batchOneSourceWithoutLink_otherStillResolved() throws ServiceException {
        var srcWith = new TwinEntity().setId(UUID.randomUUID());
        var srcWithout = new TwinEntity().setId(UUID.randomUUID());
        var linked = new TwinEntity().setId(UUID.randomUUID());
        var head = new TwinEntity();
        doAnswer(invocation -> {
            Collection<TwinEntity> twins = invocation.getArgument(0);
            for (TwinEntity t : twins) {
                var result = new TwinLinkService.FindTwinLinksResult();
                if (t == srcWith) result.getForwardLinks().add(link(linkIdValue, srcWith, linked));
                t.setTwinLinks(result);
            }
            return null;
        }).when(twinLinkService).loadTwinLinks(anyCollection());
        setupHeads(Map.of(linked, head));

        Map<UUID, TwinEntity> result = pointer.load(props(), List.of(srcWith, srcWithout));

        assertSame(head, result.get(srcWith.getId()));
        assertNull(result.get(srcWithout.getId()));
    }

    @Test
    void load_multipleForwardLinks_throwsNonSingle() throws ServiceException {
        var src = new TwinEntity().setId(UUID.randomUUID());
        var linked1 = new TwinEntity().setId(UUID.randomUUID());
        var linked2 = new TwinEntity().setId(UUID.randomUUID());
        setupLinks(link(linkIdValue, src, linked1), link(linkIdValue, src, linked2));

        var ex = assertThrows(ServiceException.class, () -> pointer.load(props(), List.of(src)));
        assertEquals(ErrorCodeTwins.POINTER_NON_SINGLE.getCode(), ex.getErrorCode());
    }

    // ----------------------------------------------------------------------------------------------
    // Failure policy: the root batch wrapper delegates TwinPointerEntity.optional to the concrete
    // impl, which skips (resolves to null) an ambiguous twin instead of throwing — so one twin can
    // no longer poison the rest of the batch.
    // ----------------------------------------------------------------------------------------------

    @Test
    void load_optionalPointerSwallowsNonSingle_andCachesNull() throws ServiceException {
        pointer.featurerService = featurerService;
        when(featurerService.extractProperties(any(Featurer.class), any(HashMap.class))).thenReturn(props());
        var src = new TwinEntity().setId(UUID.randomUUID());
        var linked1 = new TwinEntity().setId(UUID.randomUUID());
        var linked2 = new TwinEntity().setId(UUID.randomUUID());
        setupLinks(link(linkIdValue, src, linked1), link(linkIdValue, src, linked2));
        var twinPointer = new TwinPointerEntity()
                .setId(UUID.randomUUID())
                .setOptional(true)
                .setPointerParams(new HashMap<>());

        assertDoesNotThrow(() -> pointer.load(twinPointer, List.of(src)));
        assertTrue(src.hasPointer(twinPointer.getId()));
        assertNull(src.getPointer(twinPointer.getId()));
    }

    @Test
    void load_mandatoryPointerRethrowsNonSingle() throws ServiceException {
        pointer.featurerService = featurerService;
        when(featurerService.extractProperties(any(Featurer.class), any(HashMap.class))).thenReturn(props());
        var src = new TwinEntity().setId(UUID.randomUUID());
        var linked1 = new TwinEntity().setId(UUID.randomUUID());
        var linked2 = new TwinEntity().setId(UUID.randomUUID());
        setupLinks(link(linkIdValue, src, linked1), link(linkIdValue, src, linked2));
        var twinPointer = new TwinPointerEntity()
                .setId(UUID.randomUUID())
                .setOptional(false)
                .setPointerParams(new HashMap<>());

        var ex = assertThrows(ServiceException.class, () -> pointer.load(twinPointer, List.of(src)));
        assertEquals(ErrorCodeTwins.POINTER_NON_SINGLE.getCode(), ex.getErrorCode());
    }

    @Test
    void load_optionalBatch_skipsAmbiguousTwin_resolvesTheRest() throws ServiceException {
        // optional = true: src1 has 2 forward links (ambiguous) -> skipped to null, src2 has 1
        // forward link -> head2. One bad twin must not poison the rest of the batch.
        pointer.featurerService = featurerService;
        when(featurerService.extractProperties(any(Featurer.class), any(HashMap.class))).thenReturn(props());
        var src1 = new TwinEntity().setId(UUID.randomUUID());
        var src2 = new TwinEntity().setId(UUID.randomUUID());
        var linked1a = new TwinEntity().setId(UUID.randomUUID());
        var linked1b = new TwinEntity().setId(UUID.randomUUID());
        var linked2 = new TwinEntity().setId(UUID.randomUUID());
        var head2 = new TwinEntity();
        doAnswer(invocation -> {
            Collection<TwinEntity> twins = invocation.getArgument(0);
            for (TwinEntity t : twins) {
                var r = new TwinLinkService.FindTwinLinksResult();
                if (t == src1) {
                    r.getForwardLinks().add(link(linkIdValue, src1, linked1a));
                    r.getForwardLinks().add(link(linkIdValue, src1, linked1b));
                }
                if (t == src2) {
                    r.getForwardLinks().add(link(linkIdValue, src2, linked2));
                }
                t.setTwinLinks(r);
            }
            return null;
        }).when(twinLinkService).loadTwinLinks(anyCollection());
        setupHeads(Map.of(linked2, head2));
        var twinPointer = new TwinPointerEntity()
                .setId(UUID.randomUUID())
                .setOptional(true)
                .setPointerParams(new HashMap<>());

        assertDoesNotThrow(() -> pointer.load(twinPointer, List.of(src1, src2)));
        assertNull(src1.getPointer(twinPointer.getId())); // ambiguous twin skipped -> null
        assertSame(head2, src2.getPointer(twinPointer.getId())); // clean twin resolved
    }
}
