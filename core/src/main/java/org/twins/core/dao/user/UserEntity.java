package org.twins.core.dao.user;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.EasyLoggable;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorage;

import java.sql.Timestamp;
import java.util.UUID;

import static org.twins.core.service.user.UserService.maskEmail;

@Entity
@Data
@Accessors(chain = true)
@Table(name = "user")
@FieldNameConstants
public class UserEntity implements EasyLoggable, TwinFieldStorage {
    @Id
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "avatar")
    private String avatar;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "user_status_id")
    @Enumerated(EnumType.STRING)
    private UserStatus userStatusId;

    public String easyLog(Level level) {
        return "user[id:" + id + ", email:" + maskEmail(email) + "]";
    }
}

