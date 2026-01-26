package org.twins.core.dao.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.twins.core.dao.twinclass.TwinClassOwnerTypeEntity;
import org.twins.core.enums.domain.DomainType;

import java.io.Serializable;

@Entity
@Data
@Accessors(chain = true)
@FieldNameConstants
@IdClass(DomainTypeTwinClassOwnerTypeEntity.PK.class)
@Table(name = "domain_type_twin_class_owner_type")
public class DomainTypeTwinClassOwnerTypeEntity {
    @Id
    @Column(name = "domain_type_id")
    @Enumerated(EnumType.STRING)
    private DomainType domainTypeId;

    @Id
    @Column(name = "twin_class_owner_type_id")
    private String twinClassOwnerTypeId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "twin_class_owner_type_id", insertable = false, updatable = false)
    private TwinClassOwnerTypeEntity twinClassOwnerType;

    @Data
    @EqualsAndHashCode
    public static class PK implements Serializable {
        private DomainType domainTypeId;
        private String twinClassOwnerTypeId;
    }
}
