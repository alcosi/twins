package org.twins.core.dao.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;

@Entity
@Table(name = "domain_status")
@Data
@Accessors(chain = true)
public class DomainStatusEntity {

    @Id
    private String id;

    @Column(name = "description")
    private String description;

}
