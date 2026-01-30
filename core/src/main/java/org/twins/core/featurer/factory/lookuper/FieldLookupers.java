package org.twins.core.featurer.factory.lookuper;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Getter
public class FieldLookupers {
    private final FieldLookuperFromContextFields fromContextFields;
    private final FieldLookuperFromContextFieldsAndContextTwinDbFields fromContextFieldsAndContextTwinDbFields;
    private final FieldLookuperFromContextTwinDbFields fromContextTwinDbFields;
    private final FieldLookuperFromContextTwinLinkedTwinByLinkDbFields fromContextTwinLinkedByLinkTwinFields;
    private final FieldLookuperFromContextTwinLinkedTwinByFieldDbFields fromContextTwinLinkedByFieldTwinFields;
    private final FieldLookuperFromContextTwinHeadTwinDbFields fromContextTwinHeadTwinDbFields;
    private final FieldLookuperFromContextTwinUncommitedFields fromContextTwinUncommitedFields;
    private final FieldLookuperFromItemOutputDbFields fromItemOutputDbFields;
    private final FieldLookuperFromItemOutputUncommitedFields fromItemOutputUncommitedFields;
    private final FieldLookuperFromItemOutputFields fromItemOutputFields;
    private final FieldLookuperFromItemOutputHeadTwinFields fromItemOutputHeadTwinFields;
    private final FieldLookuperFromItemOutputLinkedTwinFields fromItemOutputLinkedTwinFields;
    private final FieldLookuperFromItemOutputHeadTwinLinkedTwinFields fromItemOutputHeadTwinLinkedTwinFields;
    private final FieldLookuperFromItemOutputLinkedTwinHeadTwinFields fromItemOutputLinkedTwinHeadTwinFields;
}
