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
package org.bonitasoft.web.designer.controller.export.steps;

import static org.bonitasoft.web.designer.builder.ComponentBuilder.aComponent;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.builder.WidgetBuilder.aWidget;
import static org.mockito.Mockito.*;

import org.bonitasoft.web.designer.controller.export.Zipper;
import org.bonitasoft.web.designer.utils.rule.TemporaryWidgetRepository;
import org.bonitasoft.web.designer.visitor.WidgetIdVisitor;
import org.bonitasoft.web.designer.workspace.WorkspacePathResolver;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.omg.CORBA.portable.OutputStream;

@RunWith(MockitoJUnitRunner.class)
public class WidgetsExportStepTest {

    private WorkspacePathResolver pathResolver = mock(WorkspacePathResolver.class);

    @Rule
    public TemporaryWidgetRepository repository = new TemporaryWidgetRepository(pathResolver);;

    private WidgetsExportStep step;

    @Mock
    private Zipper zipper;

    @Before
    public void beforeEach() {
        step = new WidgetsExportStep(pathResolver, new WidgetIdVisitor());
        zipper = spy(new Zipper(mock(OutputStream.class)));
    }


    @Test
    public void should_add_page_widgets_to_zip() throws Exception {
        repository.addWidget(aWidget().id("widget1"));
        repository.addWidget(aWidget().id("widget2"));

        step.execute(zipper, aPage().with(
                aComponent("widget1"),
                aComponent("widget2"))
                .build());

        verify(zipper).addToZip(repository.resolveWidgetJson("widget1"), "resources/widgets/widget1/widget1.json");
        verify(zipper).addToZip(repository.resolveWidgetJson("widget2"), "resources/widgets/widget2/widget2.json");
    }
}
