package org.twins.core.dao.error;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "error")
@Data
public class ErrorEntity {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Basic
    @Column(name = "code_local")
    public int errorCodeLocal;

    @Basic
    @Column(name = "code_external")
    public String errorCodeExternal;

    @Basic
    @Column(name = "name")
    private String name;

    @Basic
    @Column(name = "description")
    private String description;

    @Basic
    @Column(name = "client_msg_i18n_id")
    private UUID clientMsgI18nId;

//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "client_msg_i18n_id", nullable = false, insertable = false, updatable = false)
//    private I18nEntity clientMsgI18n;
}
