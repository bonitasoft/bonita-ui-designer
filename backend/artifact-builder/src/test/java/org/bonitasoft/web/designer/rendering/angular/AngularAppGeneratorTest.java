package org.bonitasoft.web.designer.rendering.angular;

import org.bonitasoft.web.designer.builder.AssetBuilder;
import org.bonitasoft.web.designer.config.WorkspaceUidProperties;
import org.bonitasoft.web.designer.model.asset.AssetScope;
import org.bonitasoft.web.designer.model.asset.AssetType;
import org.bonitasoft.web.designer.rendering.AssetHtmlBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;

import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AngularAppGeneratorTest {

    @InjectMocks
    private AngularAppGenerator angularAppGenerator;

    @Mock
    private WorkspaceUidProperties workspaceUidProperties;

    @Mock
    private AssetHtmlBuilder assetHtmlBuilder;

    @Mock
    private WidgetBundleFile widgetBundleFile;

    @Test
    public void should_generate_component_file_from_a_simple_page_artefact() {
        var page = aPage().withId("aSimplePage").build();
        var sortedAsset = Arrays.asList(AssetBuilder.anAsset().withName("myAsset.js").withType(AssetType.JAVASCRIPT).build());
        when(assetHtmlBuilder.getSortedAssets(page)).thenReturn(sortedAsset);
        when(widgetBundleFile.getWidgetsBundlePathUsedInArtifact(page)).thenReturn(Arrays.asList("aPathToBundleJs/input.js"));
        when( assetHtmlBuilder.getAssetAngularSrcList(page.getId(),AssetType.JAVASCRIPT,sortedAsset)).thenReturn(Arrays.asList("absolutePath/to/assets/js/path"));

        var componentTsContent = angularAppGenerator.generateComponentTs(page);

        assertTrue(componentTsContent.contains("import 'aPathToBundleJs/input.js';\n"));
        assertTrue(componentTsContent.contains("import 'absolutePath/to/assets/js/path';\n"));
        assertTrue(componentTsContent.contains("selector: 'aSimplePage',"));
    }

    @Test
    public void should_generate_style_file_for_a_page_with_css_assets (){
        var asset1 = AssetBuilder.anAsset().withType(AssetType.CSS).withName("pageAsset.css").withScope(AssetScope.PAGE).build();
        var asset2 = AssetBuilder.anAsset().withType(AssetType.CSS).withName("pageSecondAsset.css").withScope(AssetScope.PAGE).build();
        var page = aPage().withId("aSimplePage").withAsset(asset1,asset2).build();
        when(assetHtmlBuilder.getSortedAssets(page)).thenReturn(Arrays.asList(asset1,asset2));
        when(assetHtmlBuilder.getAssetAngularSrcList(page.getId(),AssetType.CSS,Arrays.asList(asset1,asset2)))
                .thenReturn(Arrays.asList("assets/css/pageAsset.css","assets/css/pageSecondAsset.css"));

        var styleCssContent = angularAppGenerator.generateAssetsStyle(page);

        assertTrue(styleCssContent.contains("@import 'assets/css/pageAsset.css';\n"));
        assertTrue(styleCssContent.contains("@import 'assets/css/pageSecondAsset.css';\n"));
    }

    @Test
    public void should_generate_an_empty_style_file_for_a_page_without_css_assets (){
        var page = aPage().withId("aPageWithoutAsset").build();
        when(assetHtmlBuilder.getSortedAssets(page)).thenReturn(Collections.emptyList());
        when(assetHtmlBuilder.getAssetAngularSrcList(page.getId(),AssetType.CSS,Collections.emptyList()))
                .thenReturn(Collections.emptyList());

        var styleCssContent = angularAppGenerator.generateAssetsStyle(page);

        assertTrue(styleCssContent.equals(""));

    }

    @Test
    public void should_generate_the_app_module_for_a_page (){
        var page = aPage().withId("aSimplePage").build();

        var moduleTsContent = angularAppGenerator.generateModuleTs(page);

        assertTrue(moduleTsContent.contains("export class aSimplePageModule { }"));
        assertTrue(moduleTsContent.contains("import { aSimplePage } from './aSimplePage.component';"));
        assertTrue(moduleTsContent.contains("import { PropertiesValues } from './directives/properties-values-directive';\n"));
        assertTrue(moduleTsContent.contains("import { uidModel } from './directives/uid-model-directive';\n"));

    }

    @Test
    public void should_replace_app_tag_by_page_id_when_a_page_is_generated (){
        var page = aPage().withId("aSimplePage").build();

        var indexHtmlContent = angularAppGenerator.generateFileWithAppTag(page,angularAppGenerator.INDEX_HTML_TEMPLATE);
        assertTrue(indexHtmlContent.contains(" <aSimplePage></aSimplePage"));

        var mainTsContent = angularAppGenerator.generateFileWithAppTag(page,angularAppGenerator.MAIN_TS_TEMPLATE);
        assertTrue(mainTsContent.contains("import { aSimplePageModule } from './app/aSimplePage.module';"));
    }


}
