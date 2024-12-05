package org.twins.core.featurer.factory.filler;

public enum FieldLookupMode {
    fromContextFields,
    fromContextTwinUncommitedFields,
    fromContextTwinDbFields,
    fromContextFieldsAndContextTwinDbFields,
    fromContextDbTwinFieldsAndContextFields,
    fromContextTwinHeadTwinDbFields,
    fromItemOutputDbFields
}
