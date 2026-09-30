package org.twins.core.unit.controller.rest;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import org.twins.core.controller.rest.ApiController;
import org.twins.core.dto.rest.twinstatus.TwinStatusUpdateRqDTOv1;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TWINS-934: multipart v2 endpoints deserialize via {@code ApiController.mapRequest} — the manual
 * path must enforce the same bean validation contract as the v1 {@code @RequestBody @Valid} path.
 */
class ApiControllerMapRequestTest {

    static class TestApiController extends ApiController {
        <T> T mapRequestForTest(byte[] bytes, Class<T> clazz) {
            return mapRequest(bytes, clazz);
        }
    }

    private final TestApiController controller = new TestApiController();

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = JsonMapper.builder().build();
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        ReflectionTestUtils.setField(controller, "objectMapper", objectMapper);
        ReflectionTestUtils.setField(controller, "validator", validator);
    }

    private static byte[] json(String body) {
        return body.getBytes(StandardCharsets.UTF_8);
    }

    @Test
    void malformedJsonIsRejectedAsBadRequest() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.mapRequestForTest(json("{broken"), TwinStatusUpdateRqDTOv1.class));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("Malformed request json", ex.getReason());
    }

    @Test
    void nullListIsRejectedAsBadRequest() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.mapRequestForTest(json("{}"), TwinStatusUpdateRqDTOv1.class));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("statuses"), "violation should point at the list field, got: " + ex.getReason());
    }

    @Test
    void emptyListIsRejectedAsBadRequest() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.mapRequestForTest(json("{\"statuses\":[]}"), TwinStatusUpdateRqDTOv1.class));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("statuses"), "violation should point at the list field, got: " + ex.getReason());
    }

    @Test
    void listOverBatchLimitIsRejectedAsBadRequest() {
        String elements = IntStream.range(0, 51)
                .mapToObj(i -> "{\"id\":\"" + UUID.randomUUID() + "\"}")
                .collect(Collectors.joining(","));
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> controller.mapRequestForTest(json("{\"statuses\":[" + elements + "]}"), TwinStatusUpdateRqDTOv1.class));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason().contains("statuses"), "violation should point at the list field, got: " + ex.getReason());
    }

    @Test
    void validRequestIsMappedAndReturned() {
        String body = "{\"statuses\":[{\"id\":\"" + UUID.randomUUID() + "\"},{\"id\":\"" + UUID.randomUUID() + "\"}]}";
        TwinStatusUpdateRqDTOv1 rq = controller.mapRequestForTest(json(body), TwinStatusUpdateRqDTOv1.class);
        assertEquals(2, rq.getStatuses().size());
    }
}
