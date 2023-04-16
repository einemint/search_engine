package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.model.IndexingStatus;
import searchengine.model.Site;
import searchengine.repositories.SiteRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SiteInfoService {
    @Autowired
    private SiteRepository siteRepository;

    public int saveSite(IndexingStatus status, String lastError, String url, String name) {
        Site site = new Site();
        site.setStatus(status);
        site.setStatus_time(LocalDateTime.now());
        site.setLast_error(lastError);
        site.setUrl(url);
        site.setName(name);
        int id = siteRepository.save(site).getId();

        return id;
    }

    public void updateSite(int id, IndexingStatus status, String lastError) {
        if (siteRepository.findById(id).isPresent()) {
            Site site = siteRepository.findById(id).get();
            site.setId(id);
            site.setStatus(status);
            site.setStatus_time(LocalDateTime.now());
            site.setLast_error(lastError);

            siteRepository.save(site);
        }
    }

    public int deleteSiteByUrl(String url) {
        int id = 0;
        if (siteRepository.findByUrl(url).stream().findFirst().isPresent()) {
            id = siteRepository.findByUrl(url).stream().findFirst().get().getId();
        }
        siteRepository.deleteByUrl(url);

        return id;
    }
}
