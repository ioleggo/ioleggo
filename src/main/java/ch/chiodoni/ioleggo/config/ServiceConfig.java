package ch.chiodoni.ioleggo.config;

import ch.chiodoni.ioleggo.service.GitHubStoryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {

    @Value("${github.endpoint}")
    private String endpoint;

    @Value("${github.rawendpoint}")
    private String rawendpoint;

    @Value("${github.username}")
    private String username;

    @Value("${github.password}")
    private String password;

    @Value("${github.owner}")
    private String owner;

    @Value("${github.repo}")
    private String repo;

    @Value("${github.folder}")
    private String folder;

    @Bean
    public GitHubStoryService gitHubStoryService() {
        return new GitHubStoryService(endpoint, rawendpoint, username, password, owner, repo, folder);
    }

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("stories");
    }

}
