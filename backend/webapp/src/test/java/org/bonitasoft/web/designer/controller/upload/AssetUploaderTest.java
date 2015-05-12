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
package org.bonitasoft.web.designer.controller.upload;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.PageBuilder.aFilledPage;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.repository.AssetRepository;
import org.bonitasoft.web.designer.repository.Repository;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.controller.ErrorMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

@RunWith(MockitoJUnitRunner.class)
public class AssetUploaderTest {
    @Mock
    private Repository<Page> repository;
    @Mock
    private AssetRepository<Page> assetRepository;
    @InjectMocks
    private AssetUploader assetUploader;

    @Test
    public void should_return_error_when_file_nultl() {
        ErrorMessage errorMessage = assetUploader.upload(null, "page-id", "js");
        assertThat(errorMessage.getMessage()).isEqualTo("Part named [file] is needed to successfully import a component");
    }

    @Test
    public void should_return_error_when_file_empty() {
        //We construct a mockfile (the first arg is the name of the property expected in the controller
        MockMultipartFile file = new MockMultipartFile("file", "myfile.js", "application/js", "".getBytes());

        ErrorMessage errorMessage = assetUploader.upload(file, "page-id", "js");

        assertThat(errorMessage.getMessage()).isEqualTo("Part named [file] is needed to successfully import a component");
    }

    @Test
    public void should_return_error_when_type_invalid() {
        //We construct a mockfile (the first arg is the name of the property expected in the controller
        MockMultipartFile file = new MockMultipartFile("file", "myfile.js", "application/js", "".getBytes());

        ErrorMessage errorMessage = assetUploader.upload(file, "page-id", "INVALID");

        assertThat(errorMessage.getMessage()).isEqualTo("Part named [file] is needed to successfully import a component");
    }
    

    @Test(expected = NotFoundException.class)
    public void should_throw_NotFoundException_when_page_not_exist() {
        //We construct a mockfile (the first arg is the name of the property expected in the controller
        MockMultipartFile file = new MockMultipartFile("file.js", "myfile.inv", "application/javascript", "function(){}".getBytes());
        when(repository.get("page-id")).thenThrow(new NotFoundException("not found"));

        assetUploader.upload(file, "page-id", "js");
    }

    @Test
    public void should_upload_newfile() throws Exception {
        Page page = aFilledPage("page-id");
        MockMultipartFile file = new MockMultipartFile("file.js", "myfile.inv", "application/javascript", "function(){}".getBytes());
        when(repository.get("page-id")).thenReturn(page);

        ErrorMessage errorMessage = assetUploader.upload(file, "page-id", "js");

        verify(assetRepository).save(any(Asset.class), (byte[]) any());
        verify(repository).save(page);
        assertThat(errorMessage).isNull();
    }

    @Test
    public void should_return_error_when_error_onsave() throws Exception {
        Page page = aFilledPage("page-id");
        MockMultipartFile file = new MockMultipartFile("file.js", "myfile.inv", "application/javascript", "function(){}".getBytes());
        when(repository.get("page-id")).thenReturn(page);
        doThrow(IOException.class).when(repository).save(page);
        ErrorMessage errorMessage = assetUploader.upload(file, "page-id", "js");

        assertThat(errorMessage.getMessage()).isEqualTo("Error while creating asset in myfile.inv [null]");
    }

    @Test
    public void should_upload_existingfile() throws Exception {
        Page page = aFilledPage("page-id");
        MockMultipartFile file = new MockMultipartFile("myasset.js", "myasset.js", "application/javascript", "function(){}".getBytes());
        when(repository.get("page-id")).thenReturn(page);

        ErrorMessage errorMessage = assetUploader.upload(file, "page-id", "js");

        verify(assetRepository).delete(page.getAssets().get(0));
        verify(assetRepository).save(page.getAssets().get(0), "function(){}".getBytes());
        verify(repository).save(page);
        assertThat(errorMessage).isNull();
    }

    @Test
    public void should_return_error_when_upload_existingfile_witherror() throws Exception {
        Page page = aFilledPage("page-id");
        MockMultipartFile file = new MockMultipartFile("myasset.js", "myasset.js", "application/javascript", "function(){}".getBytes());
        when(repository.get("page-id")).thenReturn(page);
        doThrow(IOException.class).when(assetRepository).delete(page.getAssets().get(0));

        ErrorMessage errorMessage = assetUploader.upload(file, "page-id", "js");

        assertThat(errorMessage.getMessage()).isEqualTo("Error while deleting asset in myasset.js [null]");
    }

}