package org.twins.core.dao.view;

import lombok.Data;

import jakarta.persistence.*;
import org.twins.core.dao.Channel;

import java.util.UUID;

@Entity
@Data
@Table(name = "view_tab_layout")
public class ViewTabLayoutEntity {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column(name = "channel_id")
    @Enumerated(EnumType.STRING)
    private Channel channel;

    @Column(name = "key")
    private String key;
}
