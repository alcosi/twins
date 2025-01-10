package org.twins.core.featurer.factory.filler;

public enum FieldLookupMode {
    fromContextFields,
    fromContextTwinUncommitedFields,
    fromContextTwinDbFields,
    fromContextFieldsAndContextTwinDbFields,
    fromContextDbTwinFieldsAndContextFields,
    fromContextTwinHeadTwinDbFields,
    fromItemOutputDbFields,
    fromItemOutputUncommitedFields,
    fromItemOutputFields, // if fromItemOutputUncommitedFields, else fromItemOutputDbFields
    fromItemOutputHeadTwinFields
}
