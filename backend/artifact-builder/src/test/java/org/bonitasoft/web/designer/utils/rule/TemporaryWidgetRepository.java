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

import org.bonitasoft.web.designer.JsonHandlerFactory;
import org.bonitasoft.web.designer.Version;
import org.bonitasoft.web.designer.builder.WidgetBuilder;
import org.bonitasoft.web.designer.config.UiDesignerProperties;
import org.bonitasoft.web.designer.config.WorkspaceProperties;
import org.bonitasoft.web.designer.livebuild.Watcher;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.BeanValidator;
import org.bonitasoft.web.designer.repository.JsonFileBasedPersister;
import org.bonitasoft.web.designer.repository.WidgetFileBasedLoader;
import org.bonitasoft.web.designer.repository.WidgetRepository;

import java.nio.file.Path;

import static java.lang.String.format;
import static org.mockito.Mockito.mock;

public class TemporaryWidgetRepository extends TemporaryFolder {

    private JsonHandler jsonHandler = new JsonHandlerFactory().create();

    private WidgetRepository repository;

    private WorkspaceProperties workspaceProperties;

    public TemporaryWidgetRepository(WorkspaceProperties workspaceProperties) {
        this.workspaceProperties = workspaceProperties;
    }

    @Override
    protected void before() throws Throwable {
        super.before();

        workspaceProperties.getWidgets().setDir(this.toPath());

        var uiDesignerProperties = new UiDesignerProperties("1.13.0", Version.MODEL_VERSION);
        uiDesignerProperties.setWorkspace(workspaceProperties);

        repository = new WidgetRepository(
                uiDesignerProperties.getWorkspace(),
                uiDesignerProperties.getWorkspaceUid(),
                new JsonFileBasedPersister<>(jsonHandler, mock(BeanValidator.class), uiDesignerProperties),
                new WidgetFileBasedLoader(jsonHandler),
                mock(BeanValidator.class),
                mock(Watcher.class),
                uiDesignerProperties
        );

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
