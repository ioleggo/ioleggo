package ch.chiodoni.ioleggo.service;

import ch.chiodoni.ioleggo.Application;
import ch.chiodoni.ioleggo.model.ResourceNotFoundException;
import ch.chiodoni.ioleggo.model.StoryFolder;
import com.codahale.metrics.Timer;
import com.squareup.okhttp.OkHttpClient;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import retrofit.ErrorHandler;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;
import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.Path;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.codahale.metrics.MetricRegistry.name;

public class GitHubStoryService {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(GitHubStoryService.class);

    private String owner;

    private String repo;

    private String folder;

    private StoryRepo storyRepo;

    private StoryFile storyFile;

    private Timer findStoryResponseTimer;
    private Timer findStoryFoldersResponseTimer;

    private static class NamedItem {
        String name;
    }

    private interface StoryRepo {
        @Headers("Accept: application/vnd.github.v3+json")
        @GET("/repos/{owner}/{repo}/contents/{folder}")
        List<NamedItem> folders(@Path("owner") String owner, @Path("repo") String repo, @Path("folder") String folder);

        @Headers("Accept: application/vnd.github.v3+json")
        @GET("/repos/{owner}/{repo}/contents/{folder}/{storyFolder}")
        List<NamedItem> stories(@Path("owner") String owner, @Path("repo") String repo, @Path("folder") String folder, @Path("storyFolder") String storyFolder);
    }

    private interface StoryFile {
        @GET("/{owner}/{repo}/master/{folder}/{storyFolder}/{title}")
        String story(@Path("owner") String owner, @Path("repo") String repo, @Path("folder") String folder, @Path("storyFolder") String storyFolder, @Path("title") String title);
    }

    private class GitHubErrorHandler implements ErrorHandler {
        @Override
        public Throwable handleError(RetrofitError cause) {
            Response response = cause.getResponse();
            if (response != null && response.getStatus() == 404) {
                return new ResourceNotFoundException();
            }
            return cause;
        }
    }

    public GitHubStoryService(String endpoint, String rawendpoint, String username, String password, String owner, String repo, String folder) {
        this.owner = owner;
        this.repo = repo;
        this.folder = folder;

        this.findStoryResponseTimer = Application.metricRegistry().timer(name(GitHubStoryService.class, "findStory"));
        this.findStoryFoldersResponseTimer = Application.metricRegistry().timer(name(GitHubStoryService.class, "findStoryFolders"));

        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.setConnectTimeout(1, TimeUnit.SECONDS);
        okHttpClient.setReadTimeout(5, TimeUnit.SECONDS);

        this.storyRepo = new RestAdapter.Builder()
                .setRequestInterceptor(new BasicAuthRequestInterceptor(username, password))
                .setClient(new OkClient(okHttpClient))
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint(endpoint)
                .setErrorHandler(new GitHubErrorHandler())
                .build().create(StoryRepo.class);

        this.storyFile = new RestAdapter.Builder()
                .setRequestInterceptor(new BasicAuthRequestInterceptor(username, password))
                .setClient(new OkClient(okHttpClient))
                .setConverter(new Converter() {
                    @Override
                    public Object fromBody(TypedInput body, Type type) throws ConversionException {
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(body.in()))) {
                            return reader.lines().reduce(
                                    (story, line) -> story.concat(" <br> ").concat(line))
                                    .orElseThrow(ResourceNotFoundException::new);
                        } catch (Exception e) {
                            throw new ConversionException(e);
                        }
                    }

                    @Override
                    public TypedOutput toBody(Object object) {
                        return null;
                    }
                })
                .setErrorHandler(new GitHubErrorHandler())
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint(rawendpoint)
                .build().create(StoryFile.class);
    }

    @Cacheable("stories")
    public List<StoryFolder> findStoryFolders() {
        final Timer.Context context = findStoryFoldersResponseTimer.time();
        try {
            LOGGER.info("Retrieving story folders");
            return storyRepo.folders(owner, repo, folder)
                    .stream()
                    .parallel()
                    .map(namedItem -> {
                        StoryFolder storyFolder = new StoryFolder(namedItem.name);
                        storyRepo.stories(owner, repo, folder, storyFolder.getFolder())
                                .forEach(story -> storyFolder.addTitle(story.name));
                        return storyFolder;
                    })
                    .collect(Collectors.toList());
        } finally {
            context.stop();
        }
    }

    @Cacheable("stories")
    public String findStory(String storyFolder, String title) {
        final Timer.Context context = findStoryResponseTimer.time();
        try {
            LOGGER.info("Retrieving {}/{}", storyFolder, title);
            return storyFile.story(owner, repo, folder, storyFolder, title);
        } finally {
            context.stop();
        }
    }

}
