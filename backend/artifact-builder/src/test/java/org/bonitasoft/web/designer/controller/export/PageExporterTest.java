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
package org.bonitasoft.web.designer.controller.export;

import org.bonitasoft.web.designer.JsonHandlerFactory;
import org.bonitasoft.web.designer.controller.export.steps.ExportStep;
import org.bonitasoft.web.designer.controller.utils.Unzipper;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.JsonViewPersistence;
import org.bonitasoft.web.designer.model.ModelException;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.service.PageService;
import org.bonitasoft.web.designer.utils.rule.TemporaryFolder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import static java.lang.String.format;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.write;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PageExporterTest {

    @Rule
    public TemporaryFolder repositoryFolder = new TemporaryFolder();

    @Mock
    private PageService pageService;

    private JsonHandler jsonHandler = new JsonHandlerFactory().create();

    private PageExporter exporter;

    private ByteArrayOutputStream artifactStream;

    @Before
    public void setUp() throws Exception {
        artifactStream = new ByteArrayOutputStream();
        exporter = new PageExporter(jsonHandler, pageService, mock(ExportStep.class));
    }

    private Page create(Page page) throws IOException {
        if (page.getId() == null) {
            page.setId("default-id");
        }
        when(pageService.get(page.getId())).thenReturn(page);
        write(repositoryFolder.toPath().resolve(format("%s.json", page.getId())), jsonHandler.toJson(page, JsonViewPersistence.class));
        return page;
    }

    private Path unzip(ByteArrayOutputStream artifactZipStream) throws IOException {
        return new Unzipper().unzipInTempDir(new ByteArrayInputStream(artifactZipStream.toByteArray()), "exportertest");
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_throw_exception_when_id_is_null() throws Exception {

        exporter.handleFileExport(null, artifactStream);
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_throw_exception_when_id_is_blank() throws Exception {

        exporter.handleFileExport(" ", artifactStream);
    }

    @Test
    public void should_throw_exception_when_artefact_to_export_is_not_found() throws Exception {
        NotFoundException cause = new NotFoundException("Page not found");
        when(pageService.get("unknown-id")).thenThrow(cause);

        Throwable throwable = catchThrowable(() ->
                exporter.handleFileExport("unknown-id", artifactStream)
        );

        assertThat(throwable)
                .isInstanceOf(ExportException.class)
                .hasCause(cause);

    }

    @Test(expected = ModelException.class)
    public void should_failed_when_page_is_not_compatible_with_product_model_version() throws Exception {
        Page page = create(aPage().withModelVersion("5.0").isCompatible(false).build());

        exporter.handleFileExport(page.getId(), artifactStream);
    }

    @Test
    public void should_fill_output_stream() throws Exception {
        Page page = create(aPage().withType("layout").withName("thelayout").build());
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        exporter.handleFileExport(page.getId(), stream);

        assertThat(stream.size()).isGreaterThan(0);
    }

    @Test
    public void should_export_json_model_of_the_exported_artefact() throws Exception {
        Page page = create(aPage().withId("myPage").build());

        exporter.handleFileExport(page.getId(), artifactStream);

        Path unzipped = unzip(artifactStream);
        byte[] actual = readAllBytes(unzipped.resolve("resources/page.json"));
        byte[] expected = readAllBytes(repositoryFolder.toPath().resolve(page.getId() + ".json"));
        assertThat(actual).isEqualTo(expected);
        deleteDirectory(unzipped.toFile());
    }

    @Test
    public void should_execute_export_steps() throws Exception {
        FakeStep fakeStep1 = new FakeStep("This is some content", "resources/file1.json");
        FakeStep fakeStep2 = new FakeStep("This is another content", "resources/deep/file2.json");
        Exporter<Page> exporter = new PageExporter(jsonHandler, pageService, fakeStep1, fakeStep2);
        Page page = create(aPage().build());

        exporter.handleFileExport(page.getId(), artifactStream);

        Path unzipped = unzip(artifactStream);
        assertThat(readAllBytes(unzipped.resolve("resources/file1.json"))).isEqualTo("This is some content".getBytes());
        assertThat(readAllBytes(unzipped.resolve("resources/deep/file2.json"))).isEqualTo("This is another content".getBytes());
        deleteDirectory(unzipped.toFile());
    }

    /**
     * Fake step that add things to zip
     */
    private class FakeStep implements ExportStep<Page> {

        private String content;

        private String filename;

        public FakeStep(String content, String filename) {
            this.content = content;
            this.filename = filename;
        }

        @Override
        public void execute(Zipper zipper, Page page) throws IOException {
            zipper.addToZip(content.getBytes(), filename);
        }
    }
}
