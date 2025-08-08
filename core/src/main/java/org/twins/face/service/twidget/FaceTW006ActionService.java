package org.twins.face.service.twidget;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.face.dao.twidget.tw006.FaceTW006ActionEntity;
import org.twins.face.dao.twidget.tw006.FaceTW006ActionRepository;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FaceTW006ActionService {

    private final FaceTW006ActionRepository faceTW006ActionRepository;

    public List<FaceTW006ActionEntity> findActionEntitiesByFaceTW006Id(UUID id) {
        return faceTW006ActionRepository.findByFaceTW006Id(id);
    }
}
