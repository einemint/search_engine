package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.model.IndexingStatus;
import searchengine.model.Site;
import searchengine.repositories.SiteRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class SiteInfoServiceImpl implements SiteInfoService{
    private SiteRepository siteRepository;
    private Site site = new Site();

    @Override
    public synchronized int saveSite(int id, IndexingStatus status, String lastError, String url, String name) {
        site.setStatus(status);
        site.setStatus_time(LocalDateTime.now());
        site.setLast_error(lastError);
        site.setUrl(url);
        site.setName(name);
        if (id == 0) {
            id = siteRepository.save(site).getId();
        }

        else {
            site.setId(id);
            siteRepository.save(site);
        }

        return id;
    }
}
