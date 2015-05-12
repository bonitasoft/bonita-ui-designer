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
package org.bonitasoft.web.designer.repository;


import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.AssetBuilder.aFilledAsset;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.NoSuchElementException;

import com.google.common.io.Files;
import org.assertj.core.api.Assertions;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.asset.AssetType;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.repository.exception.ConstraintValidationException;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.utils.rule.TemporaryFolder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AssetRepositoryTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    @Rule
    public ExpectedException exception = ExpectedException.none();

    private Path pagesPath;

    @Mock
    private BeanValidator validator;
    @Mock
    private PageRepository pageRepository;
    @InjectMocks
    private AssetRepository<Page> assetRepository;

    @Before
    public void setUp() throws Exception {
        pagesPath = Paths.get(temporaryFolder.getRoot().getPath());
    }

    @Test
    public void should_resolveAssetPath() {
        Asset<Page> asset = aFilledAsset();
        when(pageRepository.resolvePathFolder(asset.getComponent())).thenReturn(pagesPath);

        Path path = assetRepository.resolveAssetPath(asset);

        Assertions.assertThat(path.toUri()).isEqualTo(pagesPath.resolve(asset.getName()).toUri());
    }

    @Test
    public void should_not_resolveAssetPath_when_asset_invalid() {
        Asset<Page> asset = aFilledAsset();
        exception.expect(ConstraintValidationException.class);
        doThrow(ConstraintValidationException.class).when(validator).validate(asset);

        assetRepository.resolveAssetPath(asset);
    }

    @Test
    public void should_not_resolveAssetPath_when_component_in_asset_invalid() {
        Asset<Page> asset = aFilledAsset();
        exception.expect(ConstraintValidationException.class);
        doThrow(ConstraintValidationException.class).when(validator).validate(asset.getComponent());

        assetRepository.resolveAssetPath(asset);
    }

    @Test
    public void should_save_asset() throws Exception {
        Asset<Page> asset = aFilledAsset();
        Path fileExpected = pagesPath.resolve(asset.getName());
        assertThat(fileExpected.toFile()).doesNotExist();
        when(pageRepository.resolvePathFolder(asset.getComponent())).thenReturn(pagesPath);

        assetRepository.save(asset, "My example with special characters réè@# \ntest".getBytes(Charset.forName("UTF-8")));

        //A json file has to be created in the repository
        assertThat(fileExpected.toFile()).exists();
        assertThat(Files.readFirstLine(fileExpected.toFile(), Charset.forName("UTF-8"))).isEqualTo("My example with special characters réè@# ");
    }

    @Test
    public void should_delete_asset() throws Exception {
        Asset<Page> asset = aFilledAsset();
        Path fileExpected = pagesPath.resolve(asset.getName());
        temporaryFolder.newFilePath(asset.getName());
        assertThat(fileExpected.toFile()).exists();
        when(pageRepository.resolvePathFolder(asset.getComponent())).thenReturn(pagesPath);

        assetRepository.delete(asset);

        assertThat(fileExpected.toFile()).doesNotExist();
    }

    @Test(expected = NotFoundException.class)
    public void should_throw_NotFoundException_when_deleting_inexisting_page() throws Exception {
        Asset<Page> asset = aFilledAsset();
        when(pageRepository.resolvePathFolder(asset.getComponent())).thenReturn(pagesPath);

        assetRepository.delete(asset);
    }


    @Test
    public void should_get_asset() throws Exception {
        Asset<Page> asset = aFilledAsset();
        Path fileExpected = pagesPath.resolve(asset.getName());
        temporaryFolder.newFilePath(asset.getName());
        assertThat(fileExpected.toFile()).exists();
        when(pageRepository.resolvePathFolder(asset.getComponent())).thenReturn(pagesPath);

        assertThat(assetRepository.getResourceStream(asset)).isNotNull().isEmpty();
    }

    @Test(expected = NotFoundException.class)
    public void should_throw_NotFoundException_when_getting_inexisting_page() throws Exception {
        Asset<Page> asset = aFilledAsset();
        when(pageRepository.resolvePathFolder(asset.getComponent())).thenReturn(pagesPath);

        assetRepository.getResourceStream(asset);
    }


    @Test
    public void should_find_asset_used_by_a_component() throws Exception {
        Asset<Page> asset = aFilledAsset();
        pagesPath.resolve(asset.getName());
        temporaryFolder.newFilePath(asset.getName());
        when(pageRepository.resolvePathFolder(asset.getComponent())).thenReturn(pagesPath);
        when(pageRepository.get("page-id")).thenReturn(
                aPage().withAsset(asset).build());

        assertThat(assetRepository.findAsset("page-id", "myasset.js", AssetType.JAVASCRIPT).toFile()).exists();
    }

    @Test
    public void should_throw_NullPointerException_when_find_asset_with_filename_null() throws Exception {
        exception.expect(NullPointerException.class);
        exception.expectMessage("Filename is required");

        assetRepository.findAsset("page-id", null, AssetType.JAVASCRIPT);
    }

    @Test
    public void should_throw_NullPointerException_when_find_asset_with_type_null() throws Exception {
        exception.expect(NullPointerException.class);
        exception.expectMessage("Asset type is required");

        assetRepository.findAsset("page-id", "myfile.js", null);
    }

    @Test(expected = NoSuchElementException.class)
    public void should_throw_NoSuchElementException_when_finding_inexistant_asset() throws Exception {
        Asset<Page> asset = aFilledAsset();
        pagesPath.resolve(asset.getName());
        temporaryFolder.newFilePath(asset.getName());
        when(pageRepository.resolvePathFolder(asset.getComponent())).thenReturn(pagesPath);
        when(pageRepository.get("page-id")).thenReturn(aPage().withAsset(asset).build());

        assetRepository.findAsset("page-id", "inexistant.js", AssetType.JAVASCRIPT);
    }
}