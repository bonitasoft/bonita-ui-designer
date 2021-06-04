package org.bonitasoft.web.designer;

import org.bonitasoft.web.designer.controller.export.FragmentExporter;
import org.bonitasoft.web.designer.controller.export.PageExporter;
import org.bonitasoft.web.designer.controller.export.WidgetExporter;
import org.bonitasoft.web.designer.controller.importer.FragmentImporter;
import org.bonitasoft.web.designer.controller.importer.ImportStore;
import org.bonitasoft.web.designer.controller.importer.PageImporter;
import org.bonitasoft.web.designer.controller.importer.WidgetImporter;
import org.bonitasoft.web.designer.model.ModelException;
import org.bonitasoft.web.designer.rendering.HtmlGenerator;
import org.bonitasoft.web.designer.service.FragmentService;
import org.bonitasoft.web.designer.service.PageService;
import org.bonitasoft.web.designer.service.WidgetService;
import org.bonitasoft.web.designer.utils.rule.TemporaryFolder;
import org.bonitasoft.web.designer.workspace.Workspace;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.UUID;

import static org.bonitasoft.web.designer.builder.FragmentBuilder.aFragment;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.builder.WidgetBuilder.aWidget;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AngularJsArtifactBuilderTest {

    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();

    private AngularJsArtifactBuilder artifactBuilder;

    @Mock
    private PageService pageService;
    @Mock
    private FragmentService fragmentService;
    @Mock
    private WidgetService widgetService;

    @Before
    public void setUp() throws Exception {

        artifactBuilder = new AngularJsArtifactBuilder(
                // Workspace management
                mock(Workspace.class),
                widgetService,
                fragmentService,
                pageService,
                // Export
                mock(PageExporter.class),
                mock(FragmentExporter.class),
                mock(WidgetExporter.class),
                mock(HtmlGenerator.class),
                // Import
                new ImportStore(),
                mock(PageImporter.class),
                mock(FragmentImporter.class),
                mock(WidgetImporter.class)
        );
    }

    @Test
    public void build_page_should_call_page_service() throws ModelException, IOException {
        // Given
        var id = UUID.randomUUID().toString();
        when(pageService.get(id)).thenReturn(aPage().withId(id).withName(id).build());

        // When
        artifactBuilder.buildPage(id);

        // Then
        verify(pageService).get(id);
    }

    @Test
    public void build_fragment_should_call_page_service() throws ModelException, IOException {
        // Given
        var id = UUID.randomUUID().toString();
        when(fragmentService.get(id)).thenReturn(aFragment().withId(id).withName(id).build());

        // When
        artifactBuilder.buildFragment(id);

        // Then
        verify(fragmentService).get(id);
    }

    @Test
    public void build_widget_should_call_page_service() throws ModelException, IOException {
        // Given
        var id = UUID.randomUUID().toString();
        when(widgetService.get(id)).thenReturn(aWidget().withId(id).withName(id).build());

        // When
        artifactBuilder.buildWidget(id);

        // Then
        verify(widgetService).get(id);
    }
}
