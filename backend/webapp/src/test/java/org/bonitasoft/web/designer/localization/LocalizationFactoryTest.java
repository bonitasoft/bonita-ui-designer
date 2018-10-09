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

package org.bonitasoft.web.designer.localization;

import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.repository.PageRepository;
import org.bonitasoft.web.designer.utils.rule.TemporaryFolder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;

import static java.lang.String.format;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.apache.commons.io.FileUtils.writeByteArrayToFile;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LocalizationFactoryTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Mock
    PageRepository pageRepository;

    @InjectMocks
    LocalizationFactory localizationFactory;

    String localizationFileContent = "{\"fr-FR\":{\"Hello\":\"Bonjour\"}}";
    Page page = aPage().withId("page").build();
    File localizationFile;

    @Before
    public void setUp() throws Exception {
        File pageFolder = temporaryFolder.newFolder("page");
        temporaryFolder.newFolder("page", "assets", "json");
        localizationFile = temporaryFolder.newFile("page/assets/json/localization.json");
        when(pageRepository.resolvePath("page")).thenReturn(pageFolder.toPath());
    }

    @Test
    public void should_create_a_factory_which_contains_localizations() throws Exception {
        writeByteArrayToFile(localizationFile, localizationFileContent.getBytes());

        assertThat(localizationFactory.generate(page))
                .isEqualTo(createFactory(localizationFileContent));
    }

    @Test
    public void should_create_an_empty_factory_whenever_localization_file_is_not_valid_json() throws Exception {
        writeByteArrayToFile(localizationFile, "invalid json".getBytes());

        assertThat(localizationFactory.generate(page))
                .isEqualTo(createFactory("{}"));
    }

    @Test
    public void should_create_an_empty_factory_whenever_localization_file_does_not_exist() throws Exception {
        deleteQuietly(localizationFile);

        assertThat(localizationFactory.generate(page))
                .isEqualTo(createFactory("{}"));
    }

    private String createFactory(String content) {
        return format("angular.module('bonitasoft.ui.services').factory('localizationFactory', function() {" + System.lineSeparator() +
                "  return {" + System.lineSeparator() +
                "    get: function() {" + System.lineSeparator() +
                "      return %s;" + System.lineSeparator() +
                "    }" + System.lineSeparator() +
                "  };" + System.lineSeparator() +
                "});" + System.lineSeparator(), content);
    }
}
