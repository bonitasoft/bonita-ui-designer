package org.bonitasoft.web.designer.controller.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CopyResources {

    public void copyResources(Path destinationPath, String patternLocation) throws IOException {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + "/" + patternLocation + "/**");
        for (Resource resource : resources) {
            if (resource.exists() && resource.isReadable() && resource.contentLength() > 0) {
                URL url = resource.getURL();
                String urlString = url.toExternalForm();
                String targetName = urlString.substring(urlString.indexOf(patternLocation));
                File destination = new File(destinationPath.toAbsolutePath().toFile(), targetName);
                FileUtils.copyURLToFile(url, destination);
                log.debug("Copied " + url + " to " + destination.getAbsolutePath());
            } else {
                log.debug("Did not copy, seems to be directory: " + resource.getDescription());
            }
        }
    }
}
