package org.twins.core.dao.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.annotation.DataScope;
import org.twins.core.enums.datascope.DataScopeType;

@Entity
@Table(name = "domain_status")
@Data
@Accessors(chain = true)
@DataScope(DataScopeType.SYSTEM)
public class DomainStatusEntity {

    @Id
    private String id;

    @Column(name = "description")
    private String description;

}
