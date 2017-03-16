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

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.web.designer.utils.rule.TemporaryFolder;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class WatcherTest {

    private final static long SLEEP_DELAY = 20;
    private final static long POLLING_DELAY = 10;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private Watcher watcher = new Watcher();
    private Path subDirectory;

    @Before
    public void setUp() throws Exception {
        subDirectory = Files.createDirectory(folder.toPath().resolve("un repertoire"));
        watcher.setPollingDelayInMs(POLLING_DELAY);
    }

    @Test
    public void should_trigger_a_created_event_when_a_file_is_created() throws Exception {
        TestPathListener listener = new TestPathListener();
        watcher.watch(folder.toPath(), listener);

        Path file = Files.createFile(subDirectory.resolve("file"));

        Thread.sleep(SLEEP_DELAY);
        assertThat(listener.getChanged()).containsExactly(file);
    }

    @Test
    @Ignore("Ignored since it fails from time to time. Unknown root cause")
    public void should_trigger_a_modified_event_when_a_file_is_modified() throws Exception {
        Path existingFile = Files.createFile(subDirectory.resolve("file"));
        TestPathListener listener = new TestPathListener();
        watcher.watch(folder.toPath(), listener);

        Files.write(existingFile, "hello".getBytes(), StandardOpenOption.APPEND);

        Thread.sleep(SLEEP_DELAY);
        assertThat(listener.getChanged()).containsExactly(existingFile);
    }

    private class TestPathListener implements PathListener {
        final List<Path> changed = new ArrayList<>();

        @Override
        public void onChange(Path path) throws Exception {
            changed.add(path);
        }

        public List<Path> getChanged() {
            return changed;
        }
    }
}
