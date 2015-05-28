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
package org.bonitasoft.web.designer.controller.asset;

import static org.bonitasoft.web.designer.builder.AssetBuilder.anAsset;
import static org.bonitasoft.web.designer.builder.PageBuilder.aFilledPage;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.model.asset.AssetType.JAVASCRIPT;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import org.bonitasoft.web.designer.controller.exception.ServerImportException;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.repository.AssetRepository;
import org.bonitasoft.web.designer.repository.Repository;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;

@RunWith(MockitoJUnitRunner.class)
public class AssetServiceTest {
    @Mock
    private Repository<Page> repository;
    @Mock
    private AssetRepository<Page> assetRepository;
    @InjectMocks
    private AssetService assetService;
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void should_return_error_when_uploading_file_null() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(is("Part named [file] is needed to successfully import a component"));

        assetService.upload(null, aPage().withId("page-id").build(), "js");
    }

    @Test
    public void should_return_error_when_uploading_file_empty() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(is("Part named [file] is needed to successfully import a component"));

        //We construct a mockfile (the first arg is the name of the property expected in the controller
        MockMultipartFile file = new MockMultipartFile("file", "myfile.js", "application/js", "".getBytes());

        assetService.upload(file, aPage().withId("page-id").build(), "js");
    }

    @Test
    public void should_return_error_when_uploadind_type_invalid() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(is("Part named [file] is needed to successfully import a component"));

        //We construct a mockfile (the first arg is the name of the property expected in the controller
        MockMultipartFile file = new MockMultipartFile("file", "myfile.js", "application/js", "".getBytes());

        assetService.upload(file, aPage().withId("page-id").build(), "INVALID");
    }

    @Test
    public void should_upload_newfile() throws Exception {
        Page page = aFilledPage("page-id");
        MockMultipartFile file = new MockMultipartFile("file.js", "myfile.inv", "application/javascript", "function(){}".getBytes());

        assetService.upload(file, page, "js");

        verify(assetRepository).save(any(Asset.class), (byte[]) any());
        verify(repository).save(page);
    }

    @Test
    public void should_return_error_when_uploading_with_error_onsave() throws Exception {
        expectedException.expect(ServerImportException.class);
        expectedException.expectMessage(is("Error while uploading asset in myfile.inv [null]"));

        Page page = aFilledPage("page-id");
        MockMultipartFile file = new MockMultipartFile("file.js", "myfile.inv", "application/javascript", "function(){}".getBytes());
        doThrow(IOException.class).when(repository).save(page);
        assetService.upload(file, page, "js");

    }

    @Test
    public void should_upload_existing_file() throws Exception {
        Page page = aFilledPage("page-id");
        MockMultipartFile file = new MockMultipartFile("asset.js", "asset.js", "application/javascript", "function(){}".getBytes());

        assetService.upload(file, page, "js");

        verify(assetRepository).delete(page.getAssets().iterator().next());
        verify(assetRepository).save(page.getAssets().iterator().next(), "function(){}".getBytes());
        verify(repository).save(page);
    }

    @Test
    public void should_return_error_when_adding_asset_with_name_null() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(is("Asset URL is required"));
        assetService.save(aPage().withId("page-id").build(), anAsset().withName(null).build());
    }

    @Test
    public void should_return_error_when_adding_asset_with_name_empty() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(is("Asset URL is required"));
        //We construct a mockfile (the first arg is the name of the property expected in the controller
        assetService.save(aPage().withId("page-id").build(), anAsset().withName("").build());
    }

    @Test
    public void should_return_error_when_adding_asset_with_type_invalid() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(is("Asset type is required"));
        assetService.save(aPage().withId("page-id").build(), anAsset().withName("http://mycdn.com/myasset.js").withType(null).build());

    }

    @Test
    public void should_add_new_asset() throws Exception {
        Page page = aFilledPage("page-id");
        assetService.save(page, anAsset().withName("http://mycdn.com/myasset.js").withType(JAVASCRIPT).build());

        verify(repository).save(page);
    }

    @Test
    public void should_return_error_when_error_onsave() throws Exception {
        expectedException.expect(RepositoryException.class);
        Page page = aFilledPage("page-id");
        doThrow(RepositoryException.class).when(repository).save(page);
        assetService.save(page, anAsset().withName("http://mycdn.com/myasset.js").withType(JAVASCRIPT).build());
    }

    @Test
    public void should_add_existing_asset() throws Exception {
        Page page = aFilledPage("page-id");
        Asset asset = anAsset().withName("http://mycdn.com/myasset.js").withType(JAVASCRIPT).build();
        page.getAssets().add(asset);

        assetService.save(page, asset);

        verify(repository).save(page);
    }

    @Test
    public void should_not_return_error_when_adding_existing_asset_witherror_on_delete() throws Exception {
        Page page = aFilledPage("page-id");
        doThrow(IOException.class).when(assetRepository).delete(page.getAssets().iterator().next());

        assetService.save(page, anAsset().withName("http://mycdn.com/myasset.js").withType(JAVASCRIPT).build());
    }

    @Test
    public void should_return_error_when_deleting_asset_with_name_empty() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(is("Asset URL is required"));
        //We construct a mockfile (the first arg is the name of the property expected in the controller
        assetService.delete(aPage().withId("page-id").build(), anAsset().withName(null).build());
    }

    @Test
    public void should_return_error_when_deleting_asset_with_type_invalid() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(is("Asset type is required"));
        assetService.delete(aPage().withId("page-id").build(), anAsset().withName("http://mycdn.com/myasset.js").withType(null).build());

    }

    @Test
    public void should_delete_existing_asset() throws Exception {
        Page page = aFilledPage("page-id");
        Asset asset = anAsset().withName("http://mycdn.com/myasset.js").withType(JAVASCRIPT).build();
        page.getAssets().add(asset);

        assetService.delete(page, asset);

        verify(assetRepository).delete(asset);
    }
}