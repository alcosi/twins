package org.twins.core.dto.rest.twin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.datalist.DataListOptionDTOv1;
import org.twins.core.dto.rest.related.ContainsRelatedObjects;
import org.twins.core.dto.rest.related.RelatedObjectsDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassFieldDTOv1;
import org.twins.core.dto.rest.user.UserDTOv1;

import java.util.List;
import java.util.UUID;

/**
 * Class for twin client to
 */
@Data
@Accessors(chain = true)
public class TwinFieldDTOv3 implements ContainsRelatedObjects {
    @JsonIgnore()
    public String value;

    @JsonIgnore()
    public UUID twinClassFieldId;

    @JsonIgnore()
    private RelatedObjectsDTOv1 relatedObjects;

    @Override()
    public RelatedObjectsDTOv1 getRelatedObjects() {
        return relatedObjects;
    }
    
    @Override()
    public void setRelatedObjects(RelatedObjectsDTOv1 relatedObjects) {
        this.relatedObjects = relatedObjects;
    }

    public TwinClassFieldDTOv1 getTwinClassField() {
        return getRelatedObjects().get(TwinClassFieldDTOv1.class, twinClassFieldId);
    }

    public TwinDTOv2 getValueAsTwin() {
        return getFieldRelated(TwinDTOv2.class);
    }

    public UserDTOv1 getValueAsUser() {
        return getFieldRelated(UserDTOv1.class);
    }

    public DataListOptionDTOv1 getValueAsDataListOption() {
        return getFieldRelated(DataListOptionDTOv1.class);
    }

    private <T> T getFieldRelated(Class<T> clazz) {
        UUID relatedObjectId = UUID.fromString(value);
        return getRelatedObjects().get(clazz, relatedObjectId);
    }

    public List<TwinDTOv2> getValueAsTwins() {
        return getFieldRelatedObjects(TwinDTOv2.class);
    }

    public List<UserDTOv1> getValueAsUsers() {
        return getFieldRelatedObjects(UserDTOv1.class);
    }

    public List<DataListOptionDTOv1> getValueAsDataListOptions() {
        return getFieldRelatedObjects(DataListOptionDTOv1.class);
    }

    private <T> List<T> getFieldRelatedObjects(Class<T> clazz) {
        if (value == null || value.isEmpty())
            return null;
        String[] ids = value.split(",");
        var ret = new java.util.ArrayList<T>();
        for (String id : ids) {
            ret.add(getRelatedObjects().get(clazz, UUID.fromString(id)));
        }
        return ret;
    }
}
