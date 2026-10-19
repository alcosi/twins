package org.twins.core.featurer.factory.lookuper;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Getter
public class FieldLookupers {
    private final FieldLookuperFromContextFields fromContextFields;
    private final FieldLookuperFromContextFieldsAndContextTwinDbFields fromContextFieldsAndContextTwinDbFields;
    private final FieldLookuperFromContextTwinFields fromContextTwinFields;
    private final FieldLookuperFromContextTwinFieldsOnly fromContextTwinFieldsOnly;
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
        fromItemOutputLinkedTwinHeadTwinFields,
    }
    
    public FieldLookuper getByType(Type type) {
        return switch (type) {
            case fromContextFields -> this.fromContextFields;
            case fromContextFieldsAndContextTwinDbFields -> this.fromContextFieldsAndContextTwinDbFields;
            case fromContextTwinFields -> this.fromContextTwinFields;
            case fromContextTwinFieldsOnly -> this.fromContextTwinFieldsOnly;
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

    /**
     * Typed variant of {@link #getByType(Type)} for consumers of the nearest family: a wrong-family
     * value of the fieldLookuper param must fail as a configuration error, not as a ClassCastException.
     */
    public FieldLookuperNearest getNearestByType(Type type) throws ServiceException {
        FieldLookuper lookuper = getByType(type);
        if (!(lookuper instanceof FieldLookuperNearest nearest))
            throw new ServiceException(ErrorCodeCommon.FEATURER_WRONG_PARAMS,
                    "fieldLookuper value[" + type + "] is from the linked family; a nearest-family lookuper is required here");
        return nearest;
    }

    /**
     * Typed variant of {@link #getByType(Type)} for consumers of the linked family: a wrong-family
     * value of the fieldLookuper param must fail as a configuration error, not as a ClassCastException.
     */
    public FieldLookuperLinked getLinkedByType(Type type) throws ServiceException {
        FieldLookuper lookuper = getByType(type);
        if (!(lookuper instanceof FieldLookuperLinked linked))
            throw new ServiceException(ErrorCodeCommon.FEATURER_WRONG_PARAMS,
                    "fieldLookuper value[" + type + "] is from the nearest family; a linked-family lookuper is required here");
        return linked;
    }
}
