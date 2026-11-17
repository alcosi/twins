package org.twins.core.dao.action;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.twins.core.dao.annotation.DataScope;
import org.twins.core.enums.datascope.DataScopeType;

@Getter
@Setter
@Entity
@Table(name = "twin_class_field_action")
@DataScope({DataScopeType.SYSTEM, DataScopeType.DOMAIN})
public class TwinClassFieldActionEntity {

    @Id
    @Column(name = "id")
    private String id;
}
