package org.twins.core.featurer.templator;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.featurer.templator.TemplatorSimple;

import java.util.Map;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.*;


class TemplatorSimpleTest extends BaseUnitTest {

    private final TemplatorSimple templator = new TemplatorSimple();

    @Nested
    class Generate {

        @Test
        void generate_withVariables_replacesAll() throws ServiceException {
            var vars = Map.of(
                    "name", "World",
                    "greeting", "Hello"
            );

            var result = templator.generate(
                    new Properties(),
                    "${greeting} ${name}!",
                    vars
            );

            assertEquals("Hello World!", result);
        }

        @Test
        void generate_noVariables_returnsOriginalTemplate() throws ServiceException {
            var result = templator.generate(
                    new Properties(),
                    "plain text",
                    Map.of()
            );

            assertEquals("plain text", result);
        }

        @Test
        void generate_missingVariable_leavesUnreplaced() throws ServiceException {
            var vars = Map.of("name", "World");

            var result = templator.generate(
                    new Properties(),
                    "${greeting} ${name}",
                    vars
            );

            assertEquals("${greeting} World", result);
        }

        @Test
        void generate_emptyTemplate_returnsEmpty() throws ServiceException {
            var result = templator.generate(
                    new Properties(),
                    "",
                    Map.of()
            );

            assertEquals("", result);
        }
    }
}
