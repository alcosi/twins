package org.twins.core.dao.action;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.twins.core.dao.i18n.I18nEntity;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "twin_action")
public class TwinActionEntity {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "name_i18n_id")
    private UUID nameI18nId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "name_i18n_id", insertable = false, updatable = false)
    @JoinColumn(name = "name_i18n_id", insertable = false, updatable = false)
    private I18nEntity nameI18nEntity;
}
