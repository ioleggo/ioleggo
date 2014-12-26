package ch.chiodoni.ioleggo.service;

import ch.chiodoni.ioleggo.model.ResourceNotFoundException;
import ch.chiodoni.ioleggo.model.StoryFolder;
import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.io.CharStreams;
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

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.codahale.metrics.MetricRegistry.name;

public class GitHubStoryServiceOld {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(GitHubStoryServiceOld.class);
    private static final MetricRegistry METRICS = new MetricRegistry();

    private String owner;

    private String repo;

    private String folder;

    private StoryRepo storyRepo;

    private StoryFile storyFile;

    private final Timer findStoryResponseTimer = METRICS.timer(name(GitHubStoryServiceOld.class, "findStory"));
    private final Timer findStoryFoldersResponseTimer = METRICS.timer(name(GitHubStoryServiceOld.class, "findStoryFolders"));

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

    public GitHubStoryServiceOld(String endpoint, String rawendpoint, String username, String password, String owner, String repo, String folder) {
        this.owner = owner;
        this.repo = repo;
        this.folder = folder;
        ConsoleReporter reporter = ConsoleReporter.forRegistry(METRICS)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        reporter.start(1, TimeUnit.MINUTES);

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
                        try (InputStreamReader reader = new InputStreamReader(body.in())) {
                            return CharStreams.toString(reader);
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
            List<StoryFolder> storyFolders = storyRepo.folders(owner, repo, folder).stream().map(new Function<NamedItem, StoryFolder>() {
                @Override
                public StoryFolder apply(NamedItem folder) {
                    return new StoryFolder(folder.name);
                }
            }).collect(Collectors.toList());

            for (StoryFolder storyFolder : storyFolders) {
                List<NamedItem> titles = storyRepo.stories(owner, repo, folder, storyFolder.getName());
                for (NamedItem t : titles) {
                    storyFolder.addTitle(t.name);
                }
            }
            return storyFolders;
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
