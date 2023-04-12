package searchengine.services;

import searchengine.model.Page;
import searchengine.repositories.PageRepository;

public class PageInfoServiceImpl implements PageInfoService{
    private PageRepository pageRepository;
    private Page page = new Page();

    public synchronized void savePage(int siteId, String path, int code, String content) {
        page.setId(siteId);
        page.setPath(path);
        page.setCode(code);
        page.setContent(content);

        pageRepository.save(page);
    };
}
