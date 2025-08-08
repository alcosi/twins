package org.twins.core.service.system;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.twins.core.domain.system.MemoryInfoDTO;
import org.twins.core.domain.system.MemoryPoolInfoDTO;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemoryService {

    public List<MemoryInfoDTO> getMemoryInfo() {
        return MemoryCheck.getInfo().stream()
                .map(info -> new MemoryInfoDTO()
                        .setName(info.name())
                        .setUsed(info.used())
                        .setCommitted(info.committed())
                        .setMax(info.max()))
                .collect(Collectors.toList());
    }

    public List<MemoryPoolInfoDTO> getMemoryPoolInfo() {
        return MemoryPoolCheck.getInfo().stream()
                .map(info -> new MemoryPoolInfoDTO()
                        .setName(info.name())
                        .setType(info.type())
                        .setUsed(info.used()))
                .collect(Collectors.toList());
    }
}