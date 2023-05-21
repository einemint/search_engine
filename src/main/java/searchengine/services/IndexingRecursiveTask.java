package searchengine.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.UrlValidator;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.config.Referrer;
import searchengine.config.UserAgent;
import searchengine.model.IndexingStatus;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;

@Slf4j
public class IndexingRecursiveTask extends RecursiveTask<List<String>> {
    private SiteInfoService siteInfoService;
    private PageInfoService pageInfoService;
    private UserAgent userAgent;
    private Referrer referrer;
    private URL url;

    private static List<String> allLinks = new ArrayList<>();
    private static List<IndexingRecursiveTask> taskList = new ArrayList<>();


    private Document document;

    private IndexingStatus indexingStatus;
    private String lastError;

    private int siteId;
    private String siteUrl;
    private List<String> urlList;

    private String content;
    private int code;

    public IndexingRecursiveTask(SiteInfoService siteInfoService, PageInfoService pageInfoService, List<String> urlList, String siteUrl,
                                 UserAgent userAgent, Referrer referrer, int siteId) {
        this.siteInfoService = siteInfoService;
        this.pageInfoService = pageInfoService;
        this.userAgent = userAgent;
        this.referrer = referrer;

        this.urlList = urlList;

        this.siteUrl = siteUrl;
        this.siteId = siteId;
    }

    @Override
    protected List<String> compute() {
        try {
            Thread.sleep(600);
        } catch (Exception exception) {
            log.error(exception.getMessage());
        }
        boolean isMatch = isInnerPage();
        if (!isMatch) return allLinks;

        if (parseUrl(siteUrl)) {
            String formattedSiteUrl = url.getPath();
            Elements links = document.select("a[href]");

            if (!formattedSiteUrl.isEmpty() && code != 0) {
                if (pageInfoService.isExistingPage(formattedSiteUrl, siteId)) {
                    pageInfoService.savePage(siteId, formattedSiteUrl, code, content);
                    siteInfoService.updateSite(siteId, indexingStatus, lastError);
                }
            }

            if (!links.isEmpty()) {
                addTask(links);
            }
        }
        addResultsFromTasks(taskList);

        return allLinks;
    }

    private void addResultsFromTasks(List<IndexingRecursiveTask> taskList)
    {
        for (IndexingRecursiveTask task : taskList)
        {
            task.join();
        }
    }

    private void addTask(Elements links) {
        for (Element link : links) {
            String linkAddress = link.attr("abs:href");

            if (!allLinks.contains(linkAddress) && !linkAddress.contains("#")) {
                IndexingRecursiveTask task = new IndexingRecursiveTask(siteInfoService, pageInfoService, urlList, linkAddress, userAgent, referrer, siteId);
                task.fork();
                taskList.add(task);
            }

            allLinks.add(linkAddress);
        }
    }

    private boolean parseUrl(String siteUrl) {
        UrlValidator urlValidator = new UrlValidator();

        if (urlValidator.isValid(siteUrl)) {
            indexingStatus = IndexingStatus.INDEXING;
            try {
                url = new URL(siteUrl);
                Connection.Response response = Jsoup.connect(siteUrl).ignoreContentType(true).userAgent(userAgent.getUserAgent())
                        .referrer(referrer.getReferrer())
                        .execute();
                code = response.statusCode();

                if (response.statusCode() == 200) {
                    document = Jsoup.connect(siteUrl).ignoreContentType(true).userAgent(userAgent.getUserAgent())
                            .referrer(referrer.getReferrer())
                            .get();
                    content = document.html();
                    lastError = "NULL";
                }
                else {
                    content = String.valueOf(response.statusCode());
                    indexingStatus = IndexingStatus.FAILED;
                    lastError = "Ошибка индексации: страница недоступна";
                }

                return true;
            } catch (Exception exception) {
                log.error(exception.getMessage());

                return false;
            }
        }

        return false;
    }

    private boolean isInnerPage() {
        try {
            URI siteUri = new URI(siteUrl);
            String siteDomain = siteUri.getHost();
            String siteDomainName = siteDomain.startsWith("www.") ? siteDomain.substring(4) : siteDomain;

            for (String urlFromList : urlList) {
                URI urlFromListUri = new URI(urlFromList);
                String urlFromListDomain = urlFromListUri.getHost();
                String urlFromListDomainName = urlFromListDomain.startsWith("www.") ? urlFromListDomain.substring(4) : urlFromListDomain;

                if (siteDomainName.contains(urlFromListDomainName)) return true;
            }
        } catch (Exception exception) {
            log.error(exception.getMessage());
        }

        return false;
    }
}
