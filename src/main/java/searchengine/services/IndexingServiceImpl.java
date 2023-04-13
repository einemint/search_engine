package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.statistics.StartIndexingResponse;
import searchengine.dto.statistics.StopIndexingResponse;
import searchengine.engine.IndexingRecursiveTask;

import java.util.concurrent.ForkJoinPool;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {
    private ForkJoinPool forkJoinPool = new ForkJoinPool();
    private SitesList sitesList;
    private boolean isIndexing = false;

    @Override
    public StartIndexingResponse getStartIndexing() {
        StartIndexingResponse response = new StartIndexingResponse();
        if (startIndexing()) {
            response.setResult(true);
        }
        else {
            response.setResult(false);
            response.setError("Индексация уже запущена");
        }

        return response;
    }

    @Override
    public boolean startIndexing() {
        if (!sitesList.getSites().isEmpty() && !isIndexing) {
            isIndexing = true;

            for (Site site : sitesList.getSites()) {
                forkJoinPool.invoke(new IndexingRecursiveTask(site.getUrl()));

                while (forkJoinPool.isTerminating()) {
                    if (!forkJoinPool.isTerminating()) {
                        forkJoinPool.shutdown();
                        isIndexing = false;
                    }
                }
            }

            return true;
        }

        return false;
    }

    @Override
    public StopIndexingResponse getStopIndexing() {
        StopIndexingResponse response = new StopIndexingResponse();
        if (stopIndexing()) {
            response.setResult(true);
        }
        else {
            response.setResult(false);
            response.setError("Индексация не запущена");
        }

        return response;
    }

    @Override
    public boolean stopIndexing() {
        if (isIndexing) {
            if (forkJoinPool.isTerminating()) {
                forkJoinPool.shutdown();
                isIndexing = false;
            }

            return true;
        }

        return false;
    }
}
