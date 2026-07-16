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
    private final FieldLookuperFromContextTwinFields fromContextTwinFields;
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
    
    public enum Type {
        fromContextFields,
        fromContextFieldsAndContextTwinDbFields,
        fromContextTwinFields,
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
        fromItemOutputLinkedTwinHeadTwinFields,
    }
    
    public FieldLookuper getByType(Type type) {
        return switch (type) {
            case fromContextFields -> this.fromContextFields;
            case fromContextFieldsAndContextTwinDbFields -> this.fromContextFieldsAndContextTwinDbFields;
            case fromContextTwinFields -> this.fromContextTwinFields;
            case fromContextTwinDbFields -> this.fromContextTwinDbFields;
            case fromContextTwinLinkedByLinkTwinFields -> this.fromContextTwinLinkedByLinkTwinFields;
            case fromContextTwinLinkedByFieldTwinFields -> this.fromContextTwinLinkedByFieldTwinFields;
            case fromContextTwinHeadTwinDbFields -> this.fromContextTwinHeadTwinDbFields;
            case fromContextTwinUncommitedFields -> this.fromContextTwinUncommitedFields;
            case fromItemOutputDbFields -> this.fromItemOutputDbFields;
            case fromItemOutputUncommitedFields -> this.fromItemOutputUncommitedFields;
            case fromItemOutputFields -> this.fromItemOutputFields;
            case fromItemOutputHeadTwinFields -> this.fromItemOutputHeadTwinFields;
            case fromItemOutputLinkedTwinFields -> this.fromItemOutputLinkedTwinFields;
            case fromItemOutputHeadTwinLinkedTwinFields -> this.fromItemOutputHeadTwinLinkedTwinFields;
            case fromItemOutputLinkedTwinHeadTwinFields -> this.fromItemOutputLinkedTwinHeadTwinFields;
        };
    }
}
