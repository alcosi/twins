package org.twins.core.featurer.factory.filler;

public enum FieldLookupMode {
    fromContextFields,
    fromContextTwinUncommitedFields,
    fromItemOutputUncommitedFields,
    fromContextTwinDbFields,
    fromContextFieldsAndContextTwinDbFields,
    fromContextDbTwinFieldsAndContextFields,
    fromContextTwinHeadTwinDbFields,
    fromItemOutputDbFields
}
