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
package org.bonitasoft.web.designer.controller;

import static java.nio.file.Files.readAllBytes;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.bonitasoft.web.designer.model.asset.AssetType;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.repository.AssetRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(MockitoJUnitRunner.class)
public class ComponentLoaderControllerTest {

    @Mock
    private AssetRepository<Page> pageAssetRepository;
    private MockMvc mockMvc;
    private Path widgetRepositoryPath = Paths.get("src/test/resources/widgets");

    @Before
    public void setUp() {
        ComponentLoaderController componentLoaderController = new ComponentLoaderController(widgetRepositoryPath, pageAssetRepository);
        mockMvc = standaloneSetup(componentLoaderController).build();
    }

    @Test
    public void should_load_files_on_disk_according_to_asked_path() throws Exception {
        Path expectedFile = widgetRepositoryPath.resolve("pbLabel/pbLabel.js");

        mockMvc.perform(get("/generator/widgets/pbLabel/pbLabel.js"))

                .andExpect(status().isOk())
                .andExpect(content().bytes(readAllBytes(expectedFile)))
                .andExpect(header().string("Content-Length", String.valueOf(expectedFile.toFile().length())))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"pbLabel.js\""))
                .andExpect(content().encoding("UTF-8"));
    }

    @Test
    public void should_respond_404_not_found_when_file_is_not_found() throws Exception {

        mockMvc.perform(get("/generator/widgets/unknownWidget/unknownWidget.js"))

                .andExpect(status().isNotFound());

    }

    @Test
    public void should_load_asset_on_disk() throws Exception {
        Path expectedFile = widgetRepositoryPath.resolve("pbLabel/pbLabel.js");
        when(pageAssetRepository.findAsset("page-id", "asset.js", AssetType.JAVASCRIPT)).thenReturn(expectedFile);

        mockMvc.perform(get("/generator/pages/page-id/js/asset.js"))

                .andExpect(status().isOk())
                .andExpect(content().bytes(readAllBytes(expectedFile)))
                .andExpect(header().string("Content-Length", String.valueOf(expectedFile.toFile().length())))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"pbLabel.js\""))
                .andExpect(content().encoding("UTF-8"));
    }

    @Test
    public void should_respond_404_when_asset_is_not_found() throws Exception {
        when(pageAssetRepository.findAsset("page-id", "asset.js", AssetType.JAVASCRIPT)).thenReturn(null);

        mockMvc.perform(get("/generator/pages/page-id/js/asset.js")).andExpect(status().isNotFound());
    }

}
