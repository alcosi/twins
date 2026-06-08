package org.twins.core.dao.twinclass;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.util.UuidUtils;

import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@FieldNameConstants
@Table(name = "twin_class_uniqueness_field", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"twin_class_uniqueness_id", "twin_class_field_id"}, name = "twin_class_uniqueness_field_uniq")
})
public class TwinClassUniquenessFieldEntity {

    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "twin_class_uniqueness_id")
    private UUID twinClassUniquenessId;

    @Column(name = "twin_class_field_id")
    private UUID twinClassFieldId;
}
