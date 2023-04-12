package searchengine.services;

public interface PageInfoService {
    void savePage(int siteId, String path, int code, String content);
}
