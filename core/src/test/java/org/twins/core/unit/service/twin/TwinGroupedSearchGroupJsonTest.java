package org.twins.core.unit.service.twin;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.twins.core.dto.rest.twin.TwinGroupKeyDTOv1;
import org.twins.core.dto.rest.twin.TwinGroupedSearchGroupDTOv1;
import org.twins.core.enums.twin.TwinGroupValueType;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TwinGroupedSearchGroupJsonTest {
    private final ObjectMapper mapper = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    @Test
    void emptyUserKeyKeepsTypeAndNullValue() throws Exception {
        TwinGroupedSearchGroupDTOv1 dto = new TwinGroupedSearchGroupDTOv1()
                .setKey(new TwinGroupKeyDTOv1().setType(TwinGroupValueType.USER).setValue(null))
                .setCount(3L)
                .setTwinIdList(List.of());
        String json = mapper.writeValueAsString(dto);
        assertTrue(json.contains("\"type\":\"USER\""), json);
        assertTrue(json.contains("\"value\":null"), json);
        assertFalse(json.contains("linkId"), json);
    }

    @Test
    void emptyLinkKeepsLinkIdAndNullValue() throws Exception {
        UUID linkId = UUID.fromString("5fc86b22-352e-4017-9816-1f9326d8ac08");
        TwinGroupedSearchGroupDTOv1 dto = new TwinGroupedSearchGroupDTOv1()
                .setKey(new TwinGroupKeyDTOv1().setType(TwinGroupValueType.TWIN).setValue(null).setLinkId(linkId))
                .setCount(1L)
                .setTwinIdList(List.of());
        String json = mapper.writeValueAsString(dto);
        assertTrue(json.contains("\"type\":\"TWIN\""), json);
        assertTrue(json.contains("\"value\":null"), json);
        assertTrue(json.contains("\"linkId\":\"" + linkId + "\""), json);
    }
}
