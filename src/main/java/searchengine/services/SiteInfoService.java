package searchengine.services;

import searchengine.model.IndexingStatus;

public interface SiteInfoService {
    int saveSite(int id, IndexingStatus status, String lastError, String url, String name);
}
