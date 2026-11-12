package org.twins.core.dao.twin;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.twins.core.dao.annotation.DataScope;
import org.twins.core.enums.datascope.DataScopeType;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@FieldNameConstants
@Table(name = "twin_last_change")
@IdClass(TwinLastChangeEntity.Pk.class)
@DataScope(DataScopeType.DOMAIN_MEMBER)
public class TwinLastChangeEntity {

    @Id
    @Column(name = "twin_id")
    private UUID twinId;

    @Id
    @Column(name = "twin_class_field_id")
    private UUID twinClassFieldId;

    @Column(name = "last_changed_at", nullable = false)
    private Timestamp lastChangedAt;

    @Data
    public static class Pk implements Serializable {
        private UUID twinId;
        private UUID twinClassFieldId;
    }
}

