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
package org.bonitasoft.web.designer.utils.rule;

import static java.lang.String.format;
import static org.mockito.Mockito.mock;

import java.nio.file.Path;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bonitasoft.web.designer.builder.FragmentBuilder;
import org.bonitasoft.web.designer.config.UiDesignerProperties;
import org.bonitasoft.web.designer.config.WorkspaceProperties;
import org.bonitasoft.web.designer.config.WorkspaceUidProperties;
import org.bonitasoft.web.designer.livebuild.Watcher;
import org.bonitasoft.web.designer.model.JacksonObjectMapper;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.repository.BeanValidator;
import org.bonitasoft.web.designer.repository.FragmentRepository;
import org.bonitasoft.web.designer.repository.JsonFileBasedLoader;
import org.bonitasoft.web.designer.repository.JsonFileBasedPersister;

public class TemporaryFragmentRepository extends TemporaryFolder {

    private final JacksonObjectMapper objectMapper = new JacksonObjectMapper(new ObjectMapper());

    private FragmentRepository repository;

    private final WorkspaceProperties workspaceProperties;


    public TemporaryFragmentRepository(WorkspaceProperties workspaceProperties) {
        this.workspaceProperties = workspaceProperties;
    }

    @Override
    protected void before() throws Throwable {
        super.before();

        workspaceProperties.getFragments().setDir(this.toPath());

        repository = new FragmentRepository(
                workspaceProperties,
                new WorkspaceUidProperties(),
                new JsonFileBasedPersister<>(objectMapper, mock(BeanValidator.class),new UiDesignerProperties("1.13.0","2.0")),
                new JsonFileBasedLoader<>(objectMapper, Fragment.class),
                mock(BeanValidator.class),
                mock(Watcher.class));

    }

    public FragmentRepository toRepository() {
        return repository;
    }

    public Path resolveFragmentJson(String id) {
        return this.toPath().resolve(format("%s/%s.json", id, id));
    }

    public Path resolveFragmentMetadata(String id) {
        return this.toPath().resolve(format("%s/%s.metadata.json", id, id));
    }

    public Fragment addFragment(FragmentBuilder fragmentBuilder) {
        return repository.save(fragmentBuilder.build());
    }
}
