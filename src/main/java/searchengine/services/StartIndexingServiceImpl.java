package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.statistics.StartIndexingResponse;

@Service
@RequiredArgsConstructor
public class StartIndexingServiceImpl implements StartIndexingService {
    @Override
    public StartIndexingResponse getStartIndexing() {
        return new StartIndexingResponse();
    }
}
