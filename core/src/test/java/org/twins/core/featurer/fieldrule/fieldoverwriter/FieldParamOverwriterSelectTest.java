package org.twins.core.featurer.fieldrule.fieldoverwriter;

import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.twinclass.TwinClassFieldRuleEntity;
import org.twins.core.service.datalist.DataListService;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FieldParamOverwriterSelectTest extends BaseUnitTest {

    @Mock private DataListService dataListService;

    @InjectMocks private FieldParamOverwriterSelect overwriter;

    private TwinClassFieldRuleEntity rule;
    private UUID listId;

    @BeforeEach
    void setUp() {
        rule = new TwinClassFieldRuleEntity();
        listId = UUID.randomUUID();
    }

    private Properties baseProps(String multiple, String supportCustom, String longListThreshold) {
        var props = new Properties();
        props.put("listUUID", listId.toString());
        props.put("multiple", multiple != null ? multiple : "");
        props.put("supportCustom", supportCustom != null ? supportCustom : "");
        props.put("longListThreshold", longListThreshold != null ? longListThreshold : "");

        return props;
    }

    @Nested
    class GetFieldOverwriterDescriptor {

        @Test
        void getFieldOverwriterDescriptor_listSizeAboveThreshold_setsDataListIdWithoutLoadingOptions() throws ServiceException {
            when(dataListService.countByDataListId(listId)).thenReturn(100);

            var descriptor = overwriter.getFieldOverwriterDescriptor(rule, baseProps("true", "false", "10"));

            assertEquals(listId, descriptor.dataListId());
            assertNull(descriptor.options());
            assertTrue(descriptor.multiple());
            assertFalse(descriptor.supportCustom());
            verify(dataListService, never()).findByDataListId(any());
        }

        @Test
        void getFieldOverwriterDescriptor_listSizeAtOrBelowThreshold_loadsOptions() throws ServiceException {
            var options = List.of(new DataListOptionEntity().setId(UUID.randomUUID()));
            when(dataListService.countByDataListId(listId)).thenReturn(5);
            when(dataListService.findByDataListId(listId)).thenReturn(options);

            var descriptor = overwriter.getFieldOverwriterDescriptor(rule, baseProps("false", "true", "10"));

            assertNull(descriptor.dataListId());
            assertEquals(options, descriptor.options());
            assertTrue(descriptor.supportCustom());
            assertFalse(descriptor.multiple());
        }

        @Test
        void getFieldOverwriterDescriptor_validatesListIdViaDataListService() throws ServiceException {
            when(dataListService.countByDataListId(listId)).thenReturn(0);
            when(dataListService.findByDataListId(listId)).thenReturn(List.of());

            overwriter.getFieldOverwriterDescriptor(rule, baseProps("false", "false", "0"));

            verify(dataListService).checkId(listId, EntitySmartService.CheckMode.NOT_EMPTY_AND_DB_EXISTS);
        }

        @Test
        void getFieldOverwriterDescriptor_uuidSetsProvided_appliedToDescriptor() throws ServiceException {
            var optionId = UUID.randomUUID();
            var excludeId = UUID.randomUUID();
            var subsetId = UUID.randomUUID();
            var subsetExcludeId = UUID.randomUUID();
            when(dataListService.countByDataListId(listId)).thenReturn(100);

            var props = baseProps("false", "false", "10");
            props.put("dataListOptionIds", optionId.toString());
            props.put("dataListOptionExcludeIds", excludeId.toString());
            props.put("dataListSubsetIds", subsetId.toString());
            props.put("dataListSubsetIdExcludeIds", subsetExcludeId.toString());

            var descriptor = overwriter.getFieldOverwriterDescriptor(rule, props);

            assertTrue(descriptor.dataListOptionIdList().contains(optionId));
            assertTrue(descriptor.dataListOptionIdExcludeList().contains(excludeId));
            assertTrue(descriptor.dataListSubsetIdList().contains(subsetId));
            assertTrue(descriptor.dataListSubsetIdExcludeList().contains(subsetExcludeId));
        }

        @Test
        void getFieldOverwriterDescriptor_uuidSetsEmpty_descriptorListsRemainNull() throws ServiceException {
            when(dataListService.countByDataListId(listId)).thenReturn(100);

            var descriptor = overwriter.getFieldOverwriterDescriptor(rule, baseProps("false", "false", "10"));

            assertNull(descriptor.dataListOptionIdList());
            assertNull(descriptor.dataListOptionIdExcludeList());
            assertNull(descriptor.dataListSubsetIdList());
            assertNull(descriptor.dataListSubsetIdExcludeList());
        }
    }
}
