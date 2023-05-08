package searchengine.services;

import org.apache.commons.validator.routines.UrlValidator;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.config.Referrer;
import searchengine.config.UserAgent;
import searchengine.model.IndexingStatus;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class IndexingRecursiveTask extends RecursiveTask<List<String>> {
    private SiteInfoService siteInfoService;
    private PageInfoService pageInfoService;
    private UserAgent userAgent;
    private Referrer referrer;
    private URL url;
    private static List<String> allLinks = new ArrayList<>();
    private List<IndexingRecursiveTask> taskList = new ArrayList<>();


    private Document document;
    private static int siteId;
    private IndexingStatus indexingStatus;
    private String lastError;


    private String siteUrl;
    private List<String> urlList;
    private List<Matcher> matcherList = new ArrayList<>();

    private boolean isMatch = true;
    private String content;
    private int code;

    public IndexingRecursiveTask(SiteInfoService siteInfoService, PageInfoService pageInfoService, List<String> urlList, String siteUrl,
                                 UserAgent userAgent, Referrer referrer) {
        this.siteInfoService = siteInfoService;
        this.pageInfoService = pageInfoService;
        this.userAgent = userAgent;
        this.referrer = referrer;

        this.urlList = urlList;

        this.siteUrl = siteUrl;

        List<Pattern> patternList = new ArrayList<>();
        for (String urlAddress : urlList) {
            patternList.add(Pattern.compile(urlAddress));
        }
        for (Pattern pattern : patternList) {
            this.matcherList.add(pattern.matcher(siteUrl));
        }
    }

    @Override
    protected List<String> compute() {
        try {
            Thread.sleep(600);
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        if (parseUrl(siteUrl)) {
            String formattedSiteUrl = url.getPath();
            Elements links = document.select("a[href]");

            if (formattedSiteUrl.isEmpty() && isMatch && code != 0) {
                siteId = siteInfoService.saveSite(indexingStatus, lastError, siteUrl, document.title());
            } else if (!formattedSiteUrl.isEmpty() && isMatch && code != 0) {
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

            if (!linkAddress.contains("#")) {
                IndexingRecursiveTask task = new IndexingRecursiveTask(siteInfoService, pageInfoService, urlList, linkAddress, userAgent, referrer);
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
            try {;
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
                exception.printStackTrace();

                return false;
            }
        }

        return false;
    }

    private void isInnerPage() {
        for (Matcher matcher : matcherList) {
            if (matcher.find()) {
                isMatch = true;
            }
        }
    }
}
