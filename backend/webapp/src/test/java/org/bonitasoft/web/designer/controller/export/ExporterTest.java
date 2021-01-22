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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;

import org.bonitasoft.web.designer.controller.export.steps.ExportStep;
import org.bonitasoft.web.designer.controller.utils.Unzipper;
import org.bonitasoft.web.designer.model.ModelException;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.repository.PageRepository;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.service.PageService;
import org.bonitasoft.web.designer.utils.rule.TemporaryFolder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.springframework.mock.web.MockHttpServletResponse;

import static java.lang.String.format;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.write;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExporterTest {

    @Rule
    public TemporaryFolder repositoryFolder = new TemporaryFolder();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Mock
    private PageRepository pageRepository;

    @Mock
    private PageService pageService;

    private Exporter<Page> exporter;

    private MockHttpServletResponse response = new MockHttpServletResponse();

    @Before
    public void setUp() throws Exception {
        when(pageRepository.getComponentName()).thenReturn("page");

        exporter = new Exporter<>(pageRepository, pageService, mock(ExportStep.class));
    }

    private Page create(Page page) throws IOException {
        if (page.getId() == null) {
            page.setId("default-id");
        }
        lenient().when(pageRepository.get(page.getId())).thenReturn(page);
        when(pageService.get(page.getId())).thenReturn(page);
        when(pageRepository.resolvePath(page.getId())).thenReturn(repositoryFolder.toPath());
        write(repositoryFolder.toPath().resolve(format("%s.json", page.getId())), "foobar".getBytes());
        return page;
    }

    private Path unzip(MockHttpServletResponse response) throws IOException {
        return new Unzipper().unzipInTempDir(new ByteArrayInputStream(response.getContentAsByteArray()), "exportertest");
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_throw_exception_when_id_is_null() throws Exception {

        exporter.handleFileExport(null, response);
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_throw_exception_when_id_is_blank() throws Exception {

        exporter.handleFileExport(" ", response);
    }

    @Test
    public void should_throw_exception_when_artefact_to_export_is_not_found() throws Exception {
        NotFoundException cause = new NotFoundException("Page not found");
        when(pageService.get("unknown-id")).thenThrow(cause);

        exception.expect(ExportException.class);
        exception.expectCause(is(cause));

        exporter.handleFileExport("unknown-id", response);
    }

    @Test
    public void should_set_zip_content_type_to_response() throws Exception {
        Page page = create(aPage().build());

        exporter.handleFileExport(page.getId(), response);

        assertThat(response.getContentType()).isEqualTo("application/zip");
    }

    @Test(expected = ModelException.class)
    public void should_failed_when_page_is_not_compatible_with_product_model_version() throws Exception {
        Page page = create(aPage().withModelVersion("5.0").isCompatible(false).build());

        exporter.handleFileExport(page.getId(), response);
    }

    @Test
    public void should_set_formatted_filename_in_header() throws Exception {
        Page page = create(aPage().withName("é&az zer/è\"").build());

        exporter.handleFileExport(page.getId(), response);

        assertThat(response.getHeader("Content-Disposition")).isEqualTo("inline; filename=page-az-zer.zip;");
    }

    @Test
    public void should_set__artifact_type_in_filename() throws Exception {
        Page page = create(aPage().withType("layout").withName("thelayout").build());

        exporter.handleFileExport(page.getId(), response);

        assertThat(response.getHeader("Content-Disposition")).isEqualTo("inline; filename=layout-thelayout.zip;");
    }

    @Test
    public void should_export_json_model_of_the_exported_artefact() throws Exception {
        Page page = create(aPage().withId("myPage").build());

        exporter.handleFileExport(page.getId(), response);

        Path unzipped = unzip(response);
        assertThat(readAllBytes(unzipped.resolve("resources/page.json"))).isEqualTo("foobar".getBytes());
        deleteDirectory(unzipped.toFile());
    }

    @Test
    public void should_execute_export_steps() throws Exception {
        FakeStep fakeStep1 = new FakeStep("This is some content", "resources/file1.json");
        FakeStep fakeStep2 = new FakeStep("This is another content", "resources/deep/file2.json");
        Exporter<Page> exporter = new Exporter<>(pageRepository, pageService, fakeStep1, fakeStep2);
        Page page = create(aPage().build());

        exporter.handleFileExport(page.getId(), response);

        Path unzipped = unzip(response);
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
