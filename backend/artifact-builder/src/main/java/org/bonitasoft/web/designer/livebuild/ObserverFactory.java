/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.web.designer.livebuild;

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;

public class ObserverFactory {

    private static final Logger logger = LoggerFactory.getLogger(ObserverFactory.class);

    public FileAlterationObserver create(Path path, final PathListener listener) {
        var observer = new FileAlterationObserver(path.toFile());
        observer.addListener(new FileAlterationListenerAdaptor() {
            @Override
            public void onFileChange(File file) {
                // when using Files.write on an existing file, file is sometimes reset then write is done.
                // This triggers 2 change events on this file. To prevent event to be processed twice, we
                // trigger change only when file.length is greater than 0
                if (file.length() > 0L) {
                    triggerChange(file, listener);
                }
            }

            @Override
            public void onFileCreate(File file) {
                triggerChange(file, listener);
            }
        });
        return observer;
    }

    private void triggerChange(File file, PathListener listener) {
        try {
            listener.onChange(file.toPath());
        } catch (Exception e) {
            logger.error("Unexpected exception while processing file {}", file.getPath(), e);
        }
    }
}
