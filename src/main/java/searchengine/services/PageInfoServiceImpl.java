package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.model.Page;
import searchengine.repositories.PageRepository;

@Service
@RequiredArgsConstructor
public class PageInfoServiceImpl implements PageInfoService{
    private PageRepository pageRepository;
    private Page page = new Page();

    @Override
    public synchronized void savePage(int siteId, String path, int code, String content) {
        page.setId(siteId);
        page.setPath(path);
        page.setCode(code);
        page.setContent(content);

        pageRepository.save(page);
    };
}
