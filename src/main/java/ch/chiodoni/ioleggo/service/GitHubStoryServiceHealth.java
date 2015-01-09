package ch.chiodoni.ioleggo.service;

import ch.chiodoni.ioleggo.model.StoryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GitHubStoryServiceHealth implements HealthIndicator {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitHubStoryServiceHealth.class);

    @Autowired(required = true)
    private GitHubStoryService storyService;

    @Override
    public Health health() {
        List<StoryFolder> storyFolders = storyService.findStoryFolders();
        if (storyFolders == null || storyFolders.isEmpty()) {
            LOGGER.warn("GitHubStoryService is down");
            return Health.down().build();
        }
        LOGGER.info("GitHubStoryService is up");
        return Health.up().build();
    }
}
