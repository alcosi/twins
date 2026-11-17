package org.twins.core.service;

import org.cambium.common.math.IntegerRange;
import org.cambium.common.util.Ternary;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.domain.search.*;
import org.twins.core.service.comment.CommentSearchConfigService;
import org.twins.core.service.twinclass.TwinClassSearchConfigService;
import org.twins.core.service.twinclassfield.TwinClassFieldSearchConfigService;
import org.twins.core.service.user.UserSearchConfigService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ConfiguredSearchServiceNarrowingTest extends BaseUnitTest {

    // narrowSearch is protected in ConfiguredSearchService; this test declares the same package
    // to call it. The narrowing step never touches repositories/services, so nulls are fine.
    private final CommentSearchConfigService commentService = new CommentSearchConfigService(null, null, null, null);
    private final TwinClassSearchConfigService classService = new TwinClassSearchConfigService(null, null, null, null);
    private final TwinClassFieldSearchConfigService fieldService = new TwinClassFieldSearchConfigService(null, null, null, null);
    private final UserSearchConfigService userService = new UserSearchConfigService(null, null, null, null);

    @Test
    void narrowsSetFieldsByIntersection() {
        UUID a = UUID.randomUUID(), b = UUID.randomUUID(), c = UUID.randomUUID();
        TwinClassFieldSearch main = new TwinClassFieldSearch().setIdList(new HashSet<>(Set.of(a, b)));
        TwinClassFieldSearch narrow = new TwinClassFieldSearch().setIdList(new HashSet<>(Set.of(b, c)));

        fieldService.narrowSearch(main, narrow);

        assertEquals(Set.of(b), main.getIdList());
    }

    @Test
    void emptyMainTakesNarrowAndNullNarrowKeepsMain() {
        UUID a = UUID.randomUUID();

        TwinClassFieldSearch main = new TwinClassFieldSearch();
        fieldService.narrowSearch(main, new TwinClassFieldSearch().setIdList(new HashSet<>(Set.of(a))));
        assertEquals(Set.of(a), main.getIdList());

        TwinClassFieldSearch main2 = new TwinClassFieldSearch().setIdList(new HashSet<>(Set.of(a)));
        fieldService.narrowSearch(main2, new TwinClassFieldSearch());
        assertEquals(Set.of(a), main2.getIdList());
    }

    @Test
    void narrowsTernaryAndMapFields() {
        UUID a = UUID.randomUUID(), b = UUID.randomUUID();
        TwinClassFieldSearch main = new TwinClassFieldSearch().setRequired(Ternary.ONLY);
        TwinClassFieldSearch narrow = new TwinClassFieldSearch()
                .setTwinClassIdMap(new HashMap<>(Map.of(a, true, b, true)));

        fieldService.narrowSearch(main, narrow);

        assertEquals(Ternary.ONLY, main.getRequired()); // narrow is null -> main kept
        assertEquals(Map.of(a, true, b, true), main.getTwinClassIdMap()); // empty main takes narrow
    }

    @Test
    void runsSpecificHook() {
        UserSearch main = new UserSearch();
        UserSearch narrow = new UserSearch().setChildTwinSearches(new BasicSearchList());

        userService.narrowSearch(main, narrow);

        assertSame(narrow.getChildTwinSearches(), main.getChildTwinSearches());
    }

    @Test
    void nullNarrowSearchIsIgnored() {
        UUID a = UUID.randomUUID();
        CommentSearch main = new CommentSearch().setIdList(new HashSet<>(Set.of(a)));

        commentService.narrowSearch(main, null);

        assertEquals(Set.of(a), main.getIdList());
    }

    @Test
    void ignoresUnsupportedFieldTypes() {
        // ranges are marked as filter fields but not narrowed yet — the scan must skip them
        IntegerRange mainRange = new IntegerRange().setFrom(1).setTo(5);
        TwinClassSearch main = new TwinClassSearch().setTwinCounterRange(mainRange);
        TwinClassSearch narrow = new TwinClassSearch().setTwinCounterRange(new IntegerRange().setFrom(2).setTo(3));

        classService.narrowSearch(main, narrow);

        assertSame(mainRange, main.getTwinCounterRange()); // untouched

        class BadCommentSearch extends CommentSearch {
            @SearchFilter
            String text;
        }

        assertDoesNotThrow(() -> commentService.narrowSearch(new BadCommentSearch(), new BadCommentSearch()));
    }
}
