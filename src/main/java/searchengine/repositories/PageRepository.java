package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Page;

import java.util.List;

public interface PageRepository extends JpaRepository<Page, Integer> {
    List<Page> findByPathAndSiteId(String path, int siteId);
    @Transactional
    void deleteBySiteId(int siteId);
}
