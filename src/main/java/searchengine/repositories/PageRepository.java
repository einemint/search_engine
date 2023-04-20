package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Page;

public interface PageRepository extends JpaRepository<Page, Integer> {
    @Transactional
    void deleteBySiteId(int siteId);
}
