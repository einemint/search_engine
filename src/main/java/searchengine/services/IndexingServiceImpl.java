package searchengine.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Referrer;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.config.UserAgent;
import searchengine.dto.statistics.StartIndexingResponse;
import searchengine.dto.statistics.StopIndexingResponse;
import searchengine.model.IndexingStatus;

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
    private ForkJoinPool forkJoinPool;
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
        forkJoinPool = new ForkJoinPool();
        if (!sitesList.getSites().isEmpty() && !isIndexing) {
            isIndexing = true;

            urlList = sitesList.getSites().stream().map(Site::getUrl).collect(Collectors.toList());

            for (String url : urlList) {
                if (siteInfoService.getIdByUrl(url) != 0) {
                    siteInfoService.deleteById(siteInfoService.getIdByUrl(url));
                    pageInfoService.deleteBySiteId(siteInfoService.getIdByUrl(url));
                }
                addTask(url);
            }

            while (forkJoinPool.isTerminating()) {
                if (!forkJoinPool.isTerminating()) {
                    forkJoinPool.shutdown();
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
            response.setError("Индексация не запущена");
        }

        return response;
    }

    @Override
    public boolean stopIndexing() {
        if (isIndexing) {
            forkJoinPool.shutdownNow();
            isIndexing = false;
            List<searchengine.model.Site> siteList = siteInfoService.getByStatus(IndexingStatus.INDEXING);
            for (searchengine.model.Site site : siteList) {
                System.out.println(site.getId());
                siteInfoService.updateSite(site.getId(), IndexingStatus.FAILED, "Индексация остановлена пользователем");
            }
            return true;
        }

        return false;
    }

    private void addTask(String url) {
        IndexingRecursiveTask indexingRecursiveTask = new IndexingRecursiveTask(siteInfoService, pageInfoService, urlList, url, userAgent, referrer);
        forkJoinPool.invoke(indexingRecursiveTask);

        changeIndexedSiteStatus(indexingRecursiveTask, url);
    }

    private void changeIndexedSiteStatus(IndexingRecursiveTask task, String url) {
        while (forkJoinPool.isTerminating()) {
            if (task.isCompletedNormally()) {
                siteInfoService.updateSite(siteInfoService.getIdByUrl(url), IndexingStatus.INDEXED, "");
            }
            else if (task.isCompletedAbnormally() && !task.isCancelled()) {
                siteInfoService.updateSite(siteInfoService.getIdByUrl(url), IndexingStatus.FAILED, task.getException().getMessage());
            }
        }
    }
}
