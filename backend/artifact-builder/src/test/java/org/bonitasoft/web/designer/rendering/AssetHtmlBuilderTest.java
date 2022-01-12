package org.bonitasoft.web.designer.rendering;

import org.bonitasoft.web.designer.config.WorkspaceProperties;
import org.bonitasoft.web.designer.model.asset.AssetType;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.nio.file.Paths;
import java.util.Arrays;

import static org.bonitasoft.web.designer.builder.AssetBuilder.anAsset;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AssetHtmlBuilderTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @InjectMocks
    private AssetHtmlBuilder assetHtmlBuilder;

    @Mock
    private WorkspaceProperties workspaceProperties;

    @Test
    public void should_list_js_asset_from_an_artefact(){
        var pages = new WorkspaceProperties.Pages();
        pages.setDir(Paths.get(temporaryFolder.getRoot().getPath()));
        when(workspaceProperties.getPages()).thenReturn(pages);
        var assetsList= Arrays.asList(
                anAsset().withType(AssetType.JAVASCRIPT).withName("aJsAsset").build(),
                anAsset().withType(AssetType.CSS).withName("aCssAsset").build(),
                anAsset().withType(AssetType.JAVASCRIPT).withExternal(true).withName("https://aFakeCdnjs/jsFiles.min.js").build());

        var assets = assetHtmlBuilder.getAssetAngularSrcList("aPage", AssetType.JAVASCRIPT, assetsList);

        Assert.assertEquals(assets.size(),2);
        Assert.assertTrue(assets.get(0).contains(
                Paths.get(pages.getDir().toUri())
                .resolve("aPage")
                .resolve("assets")
                .resolve("js")
                .resolve("aJsAsset").toString()));
        Assert.assertEquals(assets.get(1),"https://aFakeCdnjs/jsFiles.min.js");
    }
}
