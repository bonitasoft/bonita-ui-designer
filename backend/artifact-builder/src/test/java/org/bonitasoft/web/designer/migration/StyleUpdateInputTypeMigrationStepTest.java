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
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StyleUpdateInputTypeMigrationStepTest {

    @Mock
    private AssetService<Page> pageAssetService;

    @InjectMocks
    private StyleUpdateInputTypeMigrationStep step;

    @Before
    public void setUp() throws Exception {
        step = new StyleUpdateInputTypeMigrationStep(pageAssetService);
    }

    private Asset expectedAsset(String name) {
        return anAsset().withType(CSS).withName(name).build();
    }

    @Test
    public void should_migrate_style_asset_to_only_update_control_label_required_content_property() throws Exception {
        Asset style = anAsset().withType(CSS).withName("style.css").build();
        String initContent =
                "/* Set a red border to invalid input fields in forms */\n" +
                        "input[type='text'].ng-invalid.ng-dirty, input[type='email'].ng-invalid.ng-dirty,\n" +
                        "input[type='number'].ng-invalid.ng-dirty, input[type='password'].ng-invalid.ng-dirty {\n" +
                        "  border-color: #C00;\n" +
                        "  border-width: 1px;\n" +
                        "}\n";
        Page page = aPage()
                .withDesignerVersion("1.15.12").withModelVersion("2.4").withAsset(style).build();

        when(pageAssetService.getAssetContent(page, style)).thenReturn(initContent);
        String finalContent =
                "/* Set a red border to invalid input fields in forms */\n" +
                        "input[type='text'].ng-invalid.ng-dirty, input[type='email'].ng-invalid.ng-dirty,\n" +
                        "input[type='number'].ng-invalid.ng-dirty, input[type='url'].ng-invalid.ng-dirty,\n" +
                        "input[type='password'].ng-invalid.ng-dirty {\n" +
                        "  border-color: #C00;\n" +
                        "  border-width: 1px;\n" +
                        "}\n";
        step.migrate(page);

        verify(pageAssetService).save(page, expectedAsset("style.css"), finalContent.getBytes());
    }

    @Test
    public void should_migrate_style_asset_when_only_type_text_is_exist() throws Exception {
        Asset style = anAsset().withType(CSS).withName("style.css").build();
        String initContent =
                "/* Set a red border to invalid input fields in forms */\n" +
                        "input[type='text'].ng-invalid.ng-dirty {\n" +
                        "  border-color: #C00;\n" +
                        "  border-width: 1px;\n" +
                        "}\n";

        Page page = aPage()
                .withDesignerVersion("1.15.12").withModelVersion("2.4").withAsset(style).build();

        when(pageAssetService.getAssetContent(page, style)).thenReturn(initContent);
        String finalContent =
                "/* Set a red border to invalid input fields in forms */\n" +
                        "input[type='url'].ng-invalid.ng-dirty,\n" +
                        "input[type='text'].ng-invalid.ng-dirty {\n" +
                        "  border-color: #C00;\n" +
                        "  border-width: 1px;\n" +
                        "}\n";

        step.migrate(page);

        verify(pageAssetService).save(page, expectedAsset("style.css"), finalContent.getBytes());
    }

    @Test
    public void should_not_migrate_style_when_no_css_selector_exists() throws Exception {
        Asset style = anAsset().withType(CSS).withName("style.css").build();
        String initContent =
                "/* Set a red border to invalid input fields in forms */\n";
        Page page = aPage()
                .withDesignerVersion("1.15.12").withModelVersion("2.4").withAsset(style).build();

        when(pageAssetService.getAssetContent(page, style)).thenReturn(initContent);
        String finalContent =
                "/* Set a red border to invalid input fields in forms */\n";

        step.migrate(page);

        verify(pageAssetService,never()).save(page, expectedAsset("style.css"), finalContent.getBytes());
    }

}
