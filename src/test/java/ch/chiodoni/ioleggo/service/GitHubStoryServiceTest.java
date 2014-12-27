package ch.chiodoni.ioleggo.service;

import ch.chiodoni.ioleggo.model.ResourceNotFoundException;
import ch.chiodoni.ioleggo.model.StoryFolder;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class GitHubStoryServiceTest {

    @Test
    public void testFindStoryFolders() {
        List<StoryFolder> storyFolders = gitHubStoryServiceFactory().findStoryFolders();
        Assert.assertNotNull(storyFolders);
        Assert.assertTrue(storyFolders.size() > 0);
    }

    @Test
    public void testFindStory() {
        List<StoryFolder> storyFolders = gitHubStoryServiceFactory().findStoryFolders();
        Assert.assertNotNull(storyFolders);
        Assert.assertTrue(storyFolders.size() > 0);
        String story = gitHubStoryServiceFactory().findStory(storyFolders.get(0).getFolder(), storyFolders.get(0).getTitles().get(0));
        Assert.assertNotNull(story);
        Assert.assertTrue(story.length() > 0);
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testFindNotExistingStory() {
        gitHubStoryServiceFactory().findStory("does not exist", "does not exist either");
    }

    private GitHubStoryService gitHubStoryServiceFactory() {
        return new GitHubStoryService("https://api.github.com", "https://raw.githubusercontent.com", "ioleggo", "scuolaticino2014", "ioleggo", "storie", "storie");
    }
}
