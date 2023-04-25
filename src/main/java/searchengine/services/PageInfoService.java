package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.model.Page;
import searchengine.repositories.PageRepository;

@Service
@RequiredArgsConstructor
public class PageInfoService {
    @Autowired
    private PageRepository pageRepository;

    public void savePage(int siteId, String path, int code, String content) {
        Page page = new Page();
        page.setSiteId(siteId);
        page.setPath(path);
        page.setCode(code);
        page.setContent(content);

        pageRepository.save(page);
    };

    public void deleteBySiteId(int siteId) {
        pageRepository.deleteBySiteId(siteId);
    }

    public boolean isExistingPage(String path, int siteId) {
        return pageRepository.findByPathAndSiteId(path, siteId).isEmpty();
    }
}
