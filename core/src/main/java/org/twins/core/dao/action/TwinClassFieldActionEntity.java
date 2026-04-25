package org.twins.core.dao.action;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "twin_class_field_action")
public class TwinClassFieldActionEntity {

    @Id
    @Column(name = "id")
    private String id;
}
