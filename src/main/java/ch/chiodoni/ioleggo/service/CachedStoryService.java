package ch.chiodoni.ioleggo.service;

import ch.chiodoni.ioleggo.config.CacheConfig;
import ch.chiodoni.ioleggo.model.StoryFolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("cachedStoryService")
public class CachedStoryService implements StoryService {

    @Autowired
    private StoryService storyService;

    @Override
    @Cacheable(CacheConfig.CACHE_NAME)
    public List<StoryFolder> findStoryFolders() {
        return storyService.findStoryFolders();
    }

    @Override
    @Cacheable(CacheConfig.CACHE_NAME)
    public String findStory(String storyFolder, String title) {
        return storyService.findStory(storyFolder, title);
    }

}
