package ch.chiodoni.ioleggo.web;

import ch.chiodoni.ioleggo.model.StoryFolder;
import ch.chiodoni.ioleggo.service.GitHubStoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class StoryController {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoryController.class);

//    public static final String BR = " <br> ";

    @Autowired
    private GitHubStoryService storyService;

    @RequestMapping(value = "/stories", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<StoryFolder> stories() {
        return storyService.findStoryFolders();
    }

    @ResponseBody
    @RequestMapping(value = "/story", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public String story(@RequestParam(value = "folder", required = true) String folder, @RequestParam(value = "title", required = true) String title) {
        LOGGER.info("Story {}/{} has been requested", folder, title);
        return storyService.findStory(folder, title);
    }


    /*
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
    */
}
