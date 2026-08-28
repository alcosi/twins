package org.twins.core.featurer.widget.datagrabber;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.featurer.FeaturerTwins;

import static org.junit.jupiter.api.Assertions.*;


class DataGrabberTest extends BaseUnitTest {

    private final StubDataGrabber dataGrabber = new StubDataGrabber();

    @Nested
    class TypeHierarchy {

        @Test
        void dataGrabber_extendsFeaturerTwins() {
            var grabber = new StubDataGrabber();

            assertInstanceOf(FeaturerTwins.class, grabber);
        }

        @Test
        void dataGrabber_extendsDataGrabber() {
            var grabber = new StubDataGrabber();

            assertInstanceOf(DataGrabber.class, grabber);
        }
    }

    static class StubDataGrabber extends DataGrabber {
    }
}
