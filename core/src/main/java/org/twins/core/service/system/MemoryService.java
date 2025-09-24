package org.twins.core.service.system;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.twins.core.domain.system.MemoryInfo;
import org.twins.core.domain.system.MemoryPoolInfo;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemoryService {

    public List<MemoryInfo> getMemoryInfo() {
        return MemoryCheck.getInfo().stream()
                .map(info -> new MemoryInfo()
                        .setName(info.name())
                        .setUsed(info.used())
                        .setCommitted(info.committed())
                        .setMax(info.max()))
                .collect(Collectors.toList());
    }

    public List<MemoryPoolInfo> getMemoryPoolInfo() {
        return MemoryPoolCheck.getInfo().stream()
                .map(info -> new MemoryPoolInfo()
                        .setName(info.name())
                        .setType(info.type())
                        .setUsed(info.used()))
                .collect(Collectors.toList());
    }
}
