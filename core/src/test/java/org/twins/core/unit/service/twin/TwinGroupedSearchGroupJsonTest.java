package org.twins.core.unit.service.twin;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.twins.core.dto.rest.twin.TwinGroupKeyDTOv1;
import org.twins.core.dto.rest.twin.TwinGroupedSearchGroupDTOv1;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TwinGroupedSearchGroupJsonTest {
    private final ObjectMapper mapper = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    @Test
    void nullAssignerKeyStaysInJsonAndOtherKeysStayAbsent() throws Exception {
        TwinGroupedSearchGroupDTOv1 dto = new TwinGroupedSearchGroupDTOv1()
                .setKey(new TwinGroupKeyDTOv1.Assigner().setAssignerUserId(null))
                .setCount(3L)
                .setTwins(List.of());
        String json = mapper.writeValueAsString(dto);
        assertTrue(json.contains("\"assignerUserId\":null"));
        assertFalse(json.contains("twinStatusId"));
        assertFalse(json.contains("ownerUserId"));
        assertFalse(json.contains("groupedFieldValues"));
    }

    @Test
    void nullDynamicValueStaysInsideTheFieldMap() throws Exception {
        UUID fieldId = UUID.fromString("95f2c8ca-8b79-4971-9726-270a11c7d031");
        Map<UUID, Object> values = new HashMap<>();
        values.put(fieldId, null);
        TwinGroupedSearchGroupDTOv1 dto = new TwinGroupedSearchGroupDTOv1()
                .setKey(new TwinGroupKeyDTOv1.Dynamic().setGroupedFieldValues(values))
                .setCount(1L)
                .setTwins(List.of());
        String json = mapper.writeValueAsString(dto);
        assertTrue(json.contains("\"groupedFieldValues\""), json);
        assertTrue(json.contains("\"" + fieldId + "\":null"), json);
    }

    @Test
    void emptyLinkKeepsLinkIdAndNullNode() throws Exception {
        UUID linkId = UUID.fromString("5fc86b22-352e-4017-9816-1f9326d8ac08");
        TwinGroupedSearchGroupDTOv1 dto = new TwinGroupedSearchGroupDTOv1()
                .setKey(new TwinGroupKeyDTOv1.Link().setLinkId(linkId).setLinkTwinId(null))
                .setCount(1L)
                .setTwins(List.of());
        String json = mapper.writeValueAsString(dto);
        assertTrue(json.contains("\"linkId\":\"" + linkId + "\""), json);
        assertTrue(json.contains("\"linkTwinId\":null"), json);
    }
}
