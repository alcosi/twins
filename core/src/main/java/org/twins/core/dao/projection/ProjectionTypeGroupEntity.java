package org.twins.core.dao.projection;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.util.UuidUtils;
import org.twins.core.dao.annotation.DataScope;
import org.twins.core.domain.Identifiable;
import org.twins.core.enums.datascope.DataScopeType;

import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@FieldNameConstants
@Table(name = "projection_type_group")
@DataScope(DataScopeType.DOMAIN)
public class ProjectionTypeGroupEntity implements Identifiable<UUID> {
    @Id
    private UUID id;

    @PrePersist
    protected void onCreate() {
        id = UuidUtils.ifNullGenerate(id);
    }

    @Column(name = "domain_id", nullable = false)
    private UUID domainId;

    @Column(name = "key")
    private String key;
}
