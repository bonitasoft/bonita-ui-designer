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
import static org.mockito.Mockito.when;

import java.nio.file.Path;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bonitasoft.web.designer.builder.WidgetBuilder;
import org.bonitasoft.web.designer.livebuild.Watcher;
import org.bonitasoft.web.designer.model.JacksonObjectMapper;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.*;
import org.bonitasoft.web.designer.workspace.WorkspacePathResolver;

public class TemporaryWidgetRepository extends TemporaryFolder {

    private JacksonObjectMapper objectMapper = new JacksonObjectMapper(new ObjectMapper());

    private WidgetRepository repository;

    private WorkspacePathResolver pathResolver;

    public TemporaryWidgetRepository(WorkspacePathResolver pathResolver) {
        this.pathResolver = pathResolver;
    }

    @Override
    protected void before() throws Throwable {
        super.before();

        repository = new WidgetRepository(
                this.toPath(),
                new JsonFileBasedPersister<Widget>(objectMapper, mock(BeanValidator.class)),
                new WidgetFileBasedLoader(objectMapper),
                mock(BeanValidator.class),
                mock(Watcher.class));

        when(pathResolver.getWidgetsRepositoryPath()).thenReturn(this.toPath());
    }

    public Path resolveWidgetJson(String id) {
        return this.toPath().resolve(format("%s/%s.json", id, id));
    }

    public Path resolveWidgetMetadata(String id) {
        return this.toPath().resolve(format("%s/%s.metadata.json", id, id));
    }

    public Widget addWidget(WidgetBuilder widgetBuilder) {
        return repository.save(widgetBuilder.build());
    }
}
