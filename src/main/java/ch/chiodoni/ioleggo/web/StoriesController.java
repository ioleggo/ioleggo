package ch.chiodoni.ioleggo.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.net.URLDecoder;
import java.util.*;

@RestController
public class StoriesController {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoriesController.class);

    public static final String BR = " <br> ";

    @Value("${stories.dir}")
    private String storiesDir;

    @Value("${stories.ext}")
    private String storiesExt;

    private String stories = null;

    @RequestMapping("/stories")
    public String stories() {
        if (stories == null) {
            Map<String, List<String>> storyFiles = new HashMap<>();
            scanStoryFolder(storiesDir, storyFiles);
            stories = toJSON(storyFiles);
        }
        return stories;
    }

    @RequestMapping("/story")
    public String story(@RequestParam(value = "group", required = true) String group, @RequestParam(value = "title", required = true) String title) throws IOException {
        LOGGER.info("Story {}/{} has been requested", group, title);
        String text = "";
        File file = new File(storiesDir.concat(group).concat("/").concat(title).concat(storiesExt));
        if (!file.exists()) {
            return text;
        } else {
            try (
                    InputStream is = new FileInputStream(file);
                    BufferedReader br = new BufferedReader(new InputStreamReader(is))
            ) {
                String line;
                while ((line = br.readLine()) != null) {
                    text = text.concat(line).concat(BR);
                }
                return text.substring(0, text.length() - BR.length());
            }
        }
    }

    private void scanStoryFolder(String storiesFolder, Map<String, List<String>> storyFiles) {
        List<String> files = scanStoryFolderFiles(storiesFolder);
        for (String file : files) {
            String storyFile = storiesFolder + file;
            if (new File(storyFile).isDirectory()) {
                scanStoryFolder(storyFile, storyFiles);
            } else {
                if (!storyFiles.containsKey(storiesFolder)) {
                    storyFiles.put(storiesFolder, new ArrayList<>());
                }
                storyFiles.get(storiesFolder).add(file);
            }
        }
    }

    private List<String> scanStoryFolderFiles(String storiesFolder) {
        File storyFolder = new File(storiesFolder);
        return Arrays.asList(storyFolder.list());
    }

    private String toJSON(Map<String, List<String>> storiesFolder) {
        String json = "[";
        for (String folder : storiesFolder.keySet()) {
            if (!storiesFolder.get(folder).isEmpty()) {
                json = json.concat("{\"group\":\"").concat(folder.replaceAll(storiesDir, "")).concat("\",");
                json = json.concat("\"stories\":[");
                for (String file : storiesFolder.get(folder)) {
                    json = json.concat("\"").concat(file.replaceAll(storiesExt, "")).concat("\",");
                }
                json = json.substring(0, json.length() - 1).concat("]},");
            }
        }
        return json.substring(0, json.length() - 1).concat("]");
    }
}
