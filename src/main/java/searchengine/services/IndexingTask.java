package searchengine.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.UrlValidator;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import searchengine.config.Referrer;
import searchengine.config.UserAgent;
import searchengine.model.Page;

import java.net.URI;
import java.net.URL;
import java.util.List;

@Slf4j
public class IndexingTask {
    private SiteInfoService siteInfoService;
    private PageInfoService pageInfoService;
    private final UserAgent userAgent;
    private final Referrer referrer;
    private List<String> urlList;
    private String pageUrl;
    private String pageDomainName;
    private int siteId;
    private URL url;
    private String pagePath;
    private Page page;

    public IndexingTask(SiteInfoService siteInfoService, PageInfoService pageInfoService, List<String> urlList, String pageUrl, UserAgent userAgent, Referrer referrer) {
        this.siteInfoService = siteInfoService;
        this.pageInfoService = pageInfoService;
        this.urlList = urlList;
        this.pageUrl = pageUrl;
        this.userAgent = userAgent;
        this.referrer = referrer;
    }

    public boolean indexPage() {
        return isInnerPage() && addPageToDB();
    }
    private boolean addPageToDB() {
        UrlValidator urlValidator = new UrlValidator();

        if (urlValidator.isValid(pageUrl)) {
            try {
                url = new URL(pageUrl);
                pagePath = url.getPath();
                siteId = siteInfoService.getIdByUrl(pageDomainName);

                if (!pageInfoService.isExistingPage(pagePath, siteId)) {
                    page = pageInfoService.getPageByPathAndSiteId(pagePath, siteId);
                } else {
                    page = new Page();
                }

                page = getPageData(page);
                pageInfoService.savePageFromObject(page);

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
            URI pageUri = new URI(pageUrl);
            String pageDomain = pageUri.getHost();
            pageDomainName = pageDomain.startsWith("www.") ? pageDomain.substring(4) : pageDomain;

            for (String urlFromList : urlList) {
                URI urlFromListUri = new URI(urlFromList);
                String urlFromListDomain = urlFromListUri.getHost();
                String urlFromListDomainName = urlFromListDomain.startsWith("www.") ? urlFromListDomain.substring(4) : urlFromListDomain;

                if (pageDomainName.contains(urlFromListDomainName)) return true;
            }
        } catch (Exception exception) {
            log.error(exception.getMessage());
        }

        return false;
    }

    private Page getPageData(Page page) {
        page.setSiteId(siteInfoService.getIdByUrl(pageDomainName));
        page.setPath(pagePath);

        try {
            Connection.Response response = Jsoup.connect(pageUrl).ignoreContentType(true).userAgent(userAgent.getUserAgent())
                    .referrer(referrer.getReferrer())
                    .execute();
            page.setCode(response.statusCode());

            if (response.statusCode() == 200) {

                Document document = Jsoup.connect(pageUrl).ignoreContentType(true).userAgent(userAgent.getUserAgent())
                        .referrer(referrer.getReferrer())
                        .get();
                page.setContent(document.html());
            }
            else {
                page.setContent(String.valueOf(response.statusCode()));
            }
        } catch (Exception exception) {
            log.error(exception.getMessage());
        }

        return page;
    }
}
