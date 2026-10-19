package org.twins.core.unit.featurer.factory.lookuper;

import org.junit.jupiter.api.Test;
import org.twins.core.base.BaseUnitTest;
import org.twins.core.featurer.factory.lookuper.*;

import static org.junit.jupiter.api.Assertions.assertSame;

class FieldLookupersTest extends BaseUnitTest {

    // contract: FieldLookupers is a constructor-injected holder. Each lookup bean passed in
    //           must be reachable via its matching getter (no swapping, no nulls).

    @Test
    void constructor_wiresEveryLookuperToItsGetter() {
        var fromContextFields = new FieldLookuperFromContextFields();
        var fromContextFieldsAndContextTwinDbFields = new FieldLookuperFromContextFieldsAndContextTwinDbFields();
        var fromContextTwinFields = new FieldLookuperFromContextTwinFields();
        var fromContextTwinFieldsOnly = new FieldLookuperFromContextTwinFieldsOnly();
        var fromContextTwinDbFields = new FieldLookuperFromContextTwinDbFields();
        var fromContextTwinLinkedByLinkTwinFields = new FieldLookuperFromContextTwinLinkedTwinByLinkDbFields();
        var fromContextTwinLinkedByFieldTwinFields = new FieldLookuperFromContextTwinLinkedTwinByFieldDbFields();
        var fromContextTwinHeadTwinDbFields = new FieldLookuperFromContextTwinHeadTwinDbFields();
        var fromContextTwinUncommitedFields = new FieldLookuperFromContextTwinUncommitedFields();
        var fromItemOutputDbFields = new FieldLookuperFromItemOutputDbFields();
        var fromItemOutputUncommitedFields = new FieldLookuperFromItemOutputUncommitedFields();
        var fromItemOutputFields = new FieldLookuperFromItemOutputFields();
        var fromItemOutputHeadTwinFields = new FieldLookuperFromItemOutputHeadTwinFields();
        var fromItemOutputLinkedTwinFields = new FieldLookuperFromItemOutputLinkedTwinFields();
        var fromItemOutputHeadTwinLinkedTwinFields = new FieldLookuperFromItemOutputHeadTwinLinkedTwinFields();
        var fromItemOutputLinkedTwinHeadTwinFields = new FieldLookuperFromItemOutputLinkedTwinHeadTwinFields();

        var registry = new FieldLookupers(
                fromContextFields,
                fromContextFieldsAndContextTwinDbFields,
                fromContextTwinFields,
                fromContextTwinFieldsOnly,
                fromContextTwinDbFields,
                fromContextTwinLinkedByLinkTwinFields,
                fromContextTwinLinkedByFieldTwinFields,
                fromContextTwinHeadTwinDbFields,
                fromContextTwinUncommitedFields,
                fromItemOutputDbFields,
                fromItemOutputUncommitedFields,
                fromItemOutputFields,
                fromItemOutputHeadTwinFields,
                fromItemOutputLinkedTwinFields,
                fromItemOutputHeadTwinLinkedTwinFields,
                fromItemOutputLinkedTwinHeadTwinFields);

        assertSame(fromContextFields, registry.getFromContextFields());
        assertSame(fromContextFieldsAndContextTwinDbFields, registry.getFromContextFieldsAndContextTwinDbFields());
        assertSame(fromContextTwinFields, registry.getFromContextTwinFields());
        assertSame(fromContextTwinFieldsOnly, registry.getFromContextTwinFieldsOnly());
        assertSame(fromContextTwinDbFields, registry.getFromContextTwinDbFields());
        assertSame(fromContextTwinLinkedByLinkTwinFields, registry.getFromContextTwinLinkedByLinkTwinFields());
        assertSame(fromContextTwinLinkedByFieldTwinFields, registry.getFromContextTwinLinkedByFieldTwinFields());
        assertSame(fromContextTwinHeadTwinDbFields, registry.getFromContextTwinHeadTwinDbFields());
        assertSame(fromContextTwinUncommitedFields, registry.getFromContextTwinUncommitedFields());
        assertSame(fromItemOutputDbFields, registry.getFromItemOutputDbFields());
        assertSame(fromItemOutputUncommitedFields, registry.getFromItemOutputUncommitedFields());
        assertSame(fromItemOutputFields, registry.getFromItemOutputFields());
        assertSame(fromItemOutputHeadTwinFields, registry.getFromItemOutputHeadTwinFields());
        assertSame(fromItemOutputLinkedTwinFields, registry.getFromItemOutputLinkedTwinFields());
        assertSame(fromItemOutputHeadTwinLinkedTwinFields, registry.getFromItemOutputHeadTwinLinkedTwinFields());
        assertSame(fromItemOutputLinkedTwinHeadTwinFields, registry.getFromItemOutputLinkedTwinHeadTwinFields());
    }
}
