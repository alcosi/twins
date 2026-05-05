package org.twins.core.featurer.fieldrule.fieldoverwriter;

import org.cambium.common.exception.ServiceException;
import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.dao.twinclass.TwinClassFieldRuleEntity;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class FieldParamOverwriterStubTest extends BaseUnitTest {

    private final FieldParamOverwriterStub overwriter = new FieldParamOverwriterStub();

    @Test
    void getFieldOverwriterDescriptor_anyInput_returnsNull() throws ServiceException {
        var descriptor = overwriter.getFieldOverwriterDescriptor(new TwinClassFieldRuleEntity(), new Properties());

        assertNull(descriptor);
    }
}
