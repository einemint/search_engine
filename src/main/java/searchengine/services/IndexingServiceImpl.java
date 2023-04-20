package searchengine.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Referrer;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.config.UserAgent;
import searchengine.dto.statistics.StartIndexingResponse;
import searchengine.dto.statistics.StopIndexingResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

@Service
public class IndexingServiceImpl implements IndexingService {
    @Autowired
    private SiteInfoService siteInfoService;
    @Autowired
    private PageInfoService pageInfoService;
    private ForkJoinPool forkJoinPool = new ForkJoinPool();
    @Autowired
    private SitesList sitesList;
    @Autowired
    private UserAgent userAgent;
    @Autowired
    private Referrer referrer;
    private List<String> urlList = new ArrayList<>();
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

            urlList = sitesList.getSites().stream().map(Site::getUrl).collect(Collectors.toList());

            int siteId = 0;
            for (String url : urlList) {
                siteId = siteInfoService.deleteSiteByUrl(url);
                pageInfoService.deleteBySiteId(siteId);
                addTask(url);
            }

            while (forkJoinPool.isTerminating()) {
                if (!forkJoinPool.isTerminating()) {
                    stopForkJoinPool();
                    isIndexing = false;
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
        }

        return response;
    }

    @Override
    public boolean stopIndexing() {
        if (isIndexing) {
            stopForkJoinPool();

            return true;
        }

        return false;
    }

    private void addTask(String url) {
        forkJoinPool.invoke(new IndexingRecursiveTask(siteInfoService, pageInfoService, urlList, url, userAgent, referrer));
    }

    private  void stopForkJoinPool() {
        forkJoinPool.shutdownNow();
        isIndexing = false;
    }
}
