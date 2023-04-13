package searchengine.services;

import searchengine.dto.statistics.StartIndexingResponse;
import searchengine.dto.statistics.StopIndexingResponse;

public interface IndexingService {
    boolean startIndexing();
    StartIndexingResponse getStartIndexing();
    boolean stopIndexing();
    StopIndexingResponse getStopIndexing();
}
