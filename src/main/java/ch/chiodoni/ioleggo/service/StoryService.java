package ch.chiodoni.ioleggo.service;

import ch.chiodoni.ioleggo.model.StoryFolder;

import java.util.List;

public interface StoryService {
    List<StoryFolder> findStoryFolders();

    String findStory(String storyFolder, String title);
}
