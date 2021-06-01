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
package org.bonitasoft.web.designer.migration;

import org.bonitasoft.web.designer.controller.asset.AssetService;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.page.Page;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.bonitasoft.web.designer.builder.AssetBuilder.anAsset;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.model.asset.AssetType.CSS;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StyleUpdateInputRequiredLabelMigrationStepTest {

    @Mock
    private AssetService<Page> pageAssetService;

    @InjectMocks
    private StyleUpdateInputRequiredLabelMigrationStep step;

    @Before
    public void setUp() throws Exception {
        step = new StyleUpdateInputRequiredLabelMigrationStep(pageAssetService);
    }

    private Asset expectedAsset(String name) {
        return anAsset().withType(CSS).withName(name).build();
    }

    @Test
    public void should_migrate_style_asset_to_only_update_control_label_required_content_property() throws Exception {
        Asset style = anAsset().withType(CSS).withName("style.css").build();
        String initContent = "/* Add a red star after required inputs */\n" +
                ".control-label--required:after {\n" +
                "  content: \"*\";\n" +
                "  color: #C00;\n" +
                "}" +
                "\".ping .pong {\n\" +\n" +
                " content: \"*\";\n\" +\n" +
                " color: #C00;\n\" +\n" +
                " \"}";
        Page page = aPage()
                .withDesignerVersion("1.11.30").withAsset(style).build();

        when(pageAssetService.getAssetContent(page, style)).thenReturn(initContent);
        String finalContent = "/* Add a red star after required inputs */\n" +
                ".control-label--required:after {\n" +
                "  content: \" *\";\n" +
                "  color: #C00;\n" +
                "}" +
                "\".ping .pong {\n\" +\n" +
                " content: \"*\";\n\" +\n" +
                " color: #C00;\n\" +\n" +
                " \"}";
        step.migrate(page);

        verify(pageAssetService).save(page, expectedAsset("style.css"), finalContent.getBytes());
    }

}
