package org.twins.face.dao.widget.wt003;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "face_widget_wt003")
public class FaceWT003Entity {
    @Id
    @Column(name = "face_id")
    private UUID faceId;
}
