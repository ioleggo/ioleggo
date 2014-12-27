package ch.chiodoni.ioleggo.model;

import com.google.common.collect.Ordering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StoryFolder {

    private String folder;

    private List<String> titles = new ArrayList<>();

    public StoryFolder(String folder) {
        this.folder = folder;
    }

    public StoryFolder addTitle(String title) {
        titles.add(title);
        return this;
    }

    public String getFolder() {
        return folder;
    }

    public List<String> getTitles() {
        Collections.sort(titles, Ordering.usingToString());
        return titles;
    }
}
