package searchengine.engine;

import org.springframework.beans.factory.annotation.Autowired;
import searchengine.model.IndexingStatus;

import org.apache.commons.validator.routines.UrlValidator;

import org.apache.logging.log4j.core.Logger;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.services.PageInfoService;
import searchengine.services.SiteInfoService;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IndexingRecursiveTask extends RecursiveTask<List<String>> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static volatile Logger logger;

    private static SiteInfoService siteInfoService;
    private static PageInfoService pageInfoService;

    private URL url;
    private static UrlValidator urlValidator = new UrlValidator();
    private static List<String> allLinks = new ArrayList<>();
    private List<IndexingRecursiveTask> taskList = new ArrayList<>();


    private Connection.Response response;
    private Document document;
    private static int siteId = 0;
    private IndexingStatus indexingStatus;
    private String lastError;
    private Elements links = new Elements();

    private String siteUrl;
    private String formattedSiteUrl;
    private static String siteRegex = "";
    private static Pattern pattern;
    private Matcher matcher;
    private String content;
    private int code;

    public IndexingRecursiveTask(String siteUrl) {
        this.siteUrl = siteUrl;

        if (siteRegex.isEmpty()) {
            this.siteRegex = siteUrl;
            this.pattern = Pattern.compile(siteRegex);
        }
    }

    @Override
    protected List<String> compute() {
        try {
            Thread.sleep(600);
        } catch (Exception exception) {
            logger.fatal(exception.getStackTrace());
        }

        if (parseUrl(siteUrl)) {
            code = response.statusCode();
            formattedSiteUrl = url.getPath();
            links = document.select("a[href]");
            matcher = pattern.matcher(siteUrl);

            if (!formattedSiteUrl.isEmpty() && code != 0 && matcher.find()) {
                pageInfoService.savePage(siteId, formattedSiteUrl, code, content);

                if (!links.isEmpty()) {
                    addTask(links);
                }
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
                IndexingRecursiveTask task = new IndexingRecursiveTask(linkAddress);
                task.fork();
                taskList.add(task);
            }
            allLinks.add(linkAddress);
        }
    }

    private boolean parseUrl(String siteUrl) {
        if (urlValidator.isValid(siteUrl)) {
            indexingStatus = IndexingStatus.INDEXING;
            try {
                url = new URL(siteUrl);
                response = Jsoup.connect(siteUrl).ignoreContentType(true).userAgent("Daimyo's Search Bot")
                        .referrer("http://www.google.com")
                        .execute();
                if (response.statusCode() == 200) {
                    document = Jsoup.connect(siteUrl).ignoreContentType(true).userAgent("Daimyo's Search Bot")
                            .referrer("http://www.google.com")
                            .get();
                    content = document.html();
                    lastError = "NULL";
                }
                else {
                    content = "Error status code";
                    indexingStatus = IndexingStatus.FAILED;
                    lastError = "Ошибка индексации: главная страница сайта недоступна";
                }
                siteInfoService.saveSite(siteId, indexingStatus, lastError, siteUrl, document.title());

                return true;
            } catch (Exception exception) {
                logger.fatal(exception.getStackTrace());

                return false;
            }
        }

        return false;
    }
}
