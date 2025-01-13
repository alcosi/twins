package org.twins.core.featurer.factory.lookuper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FieldLookupers {
    public final FieldLookuperFromContextFields fromContextFields;
    public final FieldLookuperFromContextFieldsAndContextTwinDbFields fromContextFieldsAndContextTwinDbFields;
    public final FieldLookuperFromContextTwinDbFields fromContextTwinDbFields;
    public final FieldLookuperFromContextTwinHeadTwinDbFields fromContextTwinHeadTwinDbFields;
    public final FieldLookuperFromContextTwinUncommitedFields fromContextTwinUncommitedFields;
    public final FieldLookuperFromItemOutputDbFields fromItemOutputDbFields;
    public final FieldLookuperFromItemOutputUncommitedFields fromItemOutputUncommitedFields;
    public final FieldLookuperFromItemOutputFields fromItemOutputFields;
    public final FieldLookuperFromItemOutputHeadTwinFields fromItemOutputHeadTwinFields;
    public final FieldLookuperFromItemOutputLinkedTwinFields fromItemOutputLinkedTwinFields;
    public final FieldLookuperFromItemOutputHeadTwinLinkedTwinFields fromItemOutputHeadTwinLinkedTwinFields;
    public final FieldLookuperFromItemOutputLinkedTwinHeadTwinFields fromItemOutputLinkedTwinHeadTwinFields;
}
