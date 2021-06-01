package org.bonitasoft.web.designer.workspace;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

@Slf4j
public class ResourcesCopier {

    public void copy(Path destinationPath, String patternLocation) throws IOException {

        var resolver = new PathMatchingResourcePatternResolver();
        var resources = resolver.getResources(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + "/" + patternLocation + "/**");
        for (var resource : resources) {
            if (resource.exists() && resource.isReadable() && resource.contentLength() > 0) {
                var url = resource.getURL();
                var urlString = url.toExternalForm();
                var targetName = urlString.substring(urlString.indexOf(patternLocation));
                var destination = new File(destinationPath.toAbsolutePath().toFile(), targetName);
                FileUtils.copyURLToFile(url, destination);
                log.debug("Copied " + url + " to " + destination.getAbsolutePath());
            } else {
                log.debug("Did not copy, seems to be directory: " + resource.getDescription());
            }
        }
    }
}
