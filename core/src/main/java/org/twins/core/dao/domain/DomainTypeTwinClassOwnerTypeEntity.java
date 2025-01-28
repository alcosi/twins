package org.twins.core.dao.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.twins.core.dao.twinclass.TwinClassOwnerTypeEntity;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Data
@Accessors(chain = true)
@FieldNameConstants
@IdClass(DomainTypeTwinClassOwnerTypeEntity.PK.class)
@Table(name = "domain_type_twin_class_owner_type")
public class DomainTypeTwinClassOwnerTypeEntity {
    @Id
    @Column(name = "domain_id")
    private UUID domainId;

    @Id
    @Column(name = "twin_class_owner_type_id")
    private String twinClassOwnerTypeId;

    @ManyToOne
    @JoinColumn(name = "twin_class_owner_type_id", insertable = false, updatable = false)
    private TwinClassOwnerTypeEntity twinClassOwnerType;

    @Data
    @EqualsAndHashCode
    public static class PK implements Serializable {
        private UUID domainId;
        private UUID twinClassOwnerTypeId;
    }
}
