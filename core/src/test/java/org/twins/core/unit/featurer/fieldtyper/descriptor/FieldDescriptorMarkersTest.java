package org.twins.core.unit.featurer.fieldtyper.descriptor;

import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorColorPicker;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorI18n;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorImmutable;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorLinkHead;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorUrl;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Covers the marker-style descriptors that carry no domain attributes of their own
 * (they only inherit {@link org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptor#backendValidated()}).
 * Their only testable contract: equal instances are equal and the inherited flag round-trips.
 */
class FieldDescriptorMarkersTest extends BaseUnitTest {

    @Test
    void immutable_equalsAndHashCode_byIdentityClass() {
        var a = new FieldDescriptorImmutable();
        var b = new FieldDescriptorImmutable();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void immutable_inheritedBackendValidated_roundTrips() {
        var d = new FieldDescriptorImmutable();
        assertFalse(d.backendValidated());
        d.backendValidated(true);
        assertTrue(d.backendValidated());
    }

    @Test
    void colorPicker_equalsSameInstance() {
        assertEquals(new FieldDescriptorColorPicker(), new FieldDescriptorColorPicker());
    }

    @Test
    void i18n_equalsSameInstance() {
        assertEquals(new FieldDescriptorI18n(), new FieldDescriptorI18n());
    }

    @Test
    void url_equalsSameInstance() {
        assertEquals(new FieldDescriptorUrl(), new FieldDescriptorUrl());
    }

    @Test
    void linkHead_equalsSameInstance() {
        assertEquals(new FieldDescriptorLinkHead(), new FieldDescriptorLinkHead());
    }
}
