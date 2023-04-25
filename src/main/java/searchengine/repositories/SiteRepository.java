package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.IndexingStatus;
import searchengine.model.Site;

import java.util.List;

public interface SiteRepository extends JpaRepository<Site, Integer> {
    List<Site> findByUrl(String url);
    @Transactional
    void deleteById(int id);
    @Transactional
    List<Site> findByStatus(IndexingStatus status);
}
