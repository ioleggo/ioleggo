package ch.chiodoni.ioleggo.web;

import ch.chiodoni.ioleggo.model.StoryFolder;
import ch.chiodoni.ioleggo.service.StoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class StoryController {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoryController.class);

    @Autowired(required = true)
    @Qualifier("cachedStoryService")
    private StoryService storyService;

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

}
