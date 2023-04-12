package searchengine.services;

import searchengine.model.IndexingStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public interface SiteInfoService {
    int saveSite(int id, IndexingStatus status, String lastError, String url, String name);
}
