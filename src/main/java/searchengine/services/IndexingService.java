package searchengine.services;

import searchengine.dto.statistics.IndexPageResponse;
import searchengine.dto.statistics.StartIndexingResponse;
import searchengine.dto.statistics.StopIndexingResponse;

public interface IndexingService {
    boolean startIndexing();
    StartIndexingResponse getStartIndexing();
    boolean stopIndexing();
    StopIndexingResponse getStopIndexing();
    boolean indexPage();
    IndexPageResponse getIndexPage(String url);
}
