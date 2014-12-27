package ch.chiodoni.ioleggo.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/cache")
public class CacheController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheController.class);

    @Autowired
    private CacheManager cacheManager;

    @RequestMapping(value = "/clear")
    public String clear() {
        LOGGER.info("Cache cleared");
        cacheManager.getCacheNames()
                .stream()
                .forEach(cache -> cacheManager.getCache(cache).clear());
        return "redirect:/";
    }

}
