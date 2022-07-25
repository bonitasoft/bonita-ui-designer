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
package org.bonitasoft.web.designer.service;

import com.fasterxml.jackson.core.FakeJsonProcessingException;
import org.bonitasoft.web.designer.builder.AssetBuilder;
import org.bonitasoft.web.designer.builder.PageBuilder;
import org.bonitasoft.web.designer.config.UiDesignerProperties;
import org.bonitasoft.web.designer.controller.MigrationStatusReport;
import org.bonitasoft.web.designer.controller.asset.AssetService;
import org.bonitasoft.web.designer.controller.asset.MalformedJsonException;
import org.bonitasoft.web.designer.model.ParameterType;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.asset.AssetScope;
import org.bonitasoft.web.designer.model.asset.AssetType;
import org.bonitasoft.web.designer.model.data.DataType;
import org.bonitasoft.web.designer.model.data.Variable;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.model.migrationReport.MigrationResult;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStatus;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStepReport;
import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.Element;
import org.bonitasoft.web.designer.model.page.FragmentElement;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.repository.PageRepository;
import org.bonitasoft.web.designer.repository.Repository;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;
import org.bonitasoft.web.designer.service.exception.IncompatibleException;
import org.bonitasoft.web.designer.visitor.AssetVisitor;
import org.bonitasoft.web.designer.visitor.ComponentVisitor;
import java.time.Instant;

import org.bonitasoft.web.designer.visitor.FragmentIdVisitor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readAllLines;
import static java.time.format.DateTimeFormatter.*;
import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.*;
import static org.bonitasoft.web.designer.builder.AssetBuilder.anAsset;
import static org.bonitasoft.web.designer.builder.ComponentBuilder.aComponent;
import static org.bonitasoft.web.designer.builder.FragmentBuilder.aFragment;
import static org.bonitasoft.web.designer.builder.PageBuilder.aFilledPage;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.builder.PageWithFragmentBuilder.aPageWithFragmentElement;
import static org.bonitasoft.web.designer.controller.asset.AssetService.OrderType.DECREMENT;
import static org.bonitasoft.web.designer.controller.asset.AssetService.OrderType.INCREMENT;
import static org.bonitasoft.web.designer.model.asset.AssetScope.PAGE;
import static org.bonitasoft.web.designer.model.asset.AssetType.JAVASCRIPT;
import static org.bonitasoft.web.designer.model.data.DataType.URL;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PageServiceTest {

    private static final String CURRENT_MODEL_VERSION = "2.0";

    @Mock
    private PageMigrationApplyer pageMigrationApplyer;

    @Mock
    private PageRepository pageRepository;

    @Mock
    private ComponentVisitor componentVisitor;

    @Mock
    private AssetVisitor assetVisitor;

    @Mock
    private FragmentIdVisitor fragmentIdVisitor;

    @Mock
    private AssetService<Page> pageAssetService;

    private DefaultPageService pageService;

    @Mock
    private DefaultFragmentService fragmentService;

    private MigrationStatusReport defaultStatusReport;

    @Before
    public void setUp() throws Exception {
        pageService = spy(new DefaultPageService(
                pageRepository,
                pageMigrationApplyer,
                componentVisitor,
                assetVisitor,
                fragmentIdVisitor,
                fragmentService,
                new UiDesignerProperties("1.13.0", CURRENT_MODEL_VERSION),
                pageAssetService
        ));
        defaultStatusReport = new MigrationStatusReport(true, false);
        doReturn(defaultStatusReport).when(pageService).getStatus(any());
        when(pageRepository.updateLastUpdateAndSave(any())).thenAnswer(invocation -> invocation.getArgument(0));

    }

    @Test
    public void should_migrate_found_page_when_get_is_called() {
        Page page = PageBuilder.aPage().withId("myPage").withDesignerVersion("1.0.0").build();
        Page migratedPage = PageBuilder.aPage().withId("myPage").withModelVersion("2.0").withPreviousArtifactVersion("1.0.0").build();
        when(pageRepository.get("myPage")).thenReturn(page);
        MigrationResult mr = new MigrationResult(migratedPage, asList(new MigrationStepReport(MigrationStatus.SUCCESS)));
        when(pageMigrationApplyer.migrate(page)).thenReturn(mr);
        doReturn(new MigrationStatusReport(true, true)).when(pageService).getStatus(any());

        pageService.get("myPage");

        verify(pageMigrationApplyer).migrate(page);
        verify(pageRepository).updateLastUpdateAndSave((Page) mr.getArtifact());
    }

    @Test
    public void should_not_update_and_save_page_if_no_migration_done() {
        Page page = PageBuilder.aPage().withId("myPage").withDesignerVersion("2.0").build();
        when(pageRepository.get("myPage")).thenReturn(page);

        Page returnedPage = pageService.get("myPage");

        verify(pageMigrationApplyer, never()).migrate(page);
        verify(pageRepository, never()).updateLastUpdateAndSave(any(Page.class));
        assertTrue(returnedPage.getStatus().isCompatible());
        assertFalse(returnedPage.getStatus().isMigration());
    }

    @Test
    public void should_not_update_and_save_page_if_migration_is_on_error() {
        Page page = PageBuilder.aPage().withId("myPage").withDesignerVersion("1.0.0").build();
        Page migratedPage = PageBuilder.aPage().withId("myPage").withModelVersion("2.0").withPreviousArtifactVersion("1.0.0").build();
        when(pageRepository.get("myPage")).thenReturn(page);
        MigrationResult mr = new MigrationResult(migratedPage, asList(new MigrationStepReport(MigrationStatus.ERROR)));
        when(pageMigrationApplyer.migrate(page)).thenReturn(mr);
        doReturn(new MigrationStatusReport(true, true)).when(pageService).getStatus(any());

        pageService.get("myPage");

        verify(pageMigrationApplyer).migrate(page);
        verify(pageRepository, never()).updateLastUpdateAndSave((Page) mr.getArtifact());
    }

    @Test
    public void should_migrate_page_when_dependencies_need_to_be_migrated() {
        Page page = PageBuilder.aPage().withId("myPage").withModelVersion("2.0").build();
        Page migratedPage = PageBuilder.aPage().withId("myPage").withModelVersion("2.0").withPreviousArtifactVersion("1.0.0").build();
        when(pageRepository.get("myPage")).thenReturn(page);
        MigrationResult mr = new MigrationResult(migratedPage, asList(new MigrationStepReport(MigrationStatus.SUCCESS)));
        when(pageMigrationApplyer.migrate(page)).thenReturn(mr);
        doReturn(new MigrationStatusReport(true, true)).when(pageService).getStatus(any());

        pageService.get("myPage");

        verify(pageMigrationApplyer).migrate(page);
        verify(pageRepository).updateLastUpdateAndSave((Page) mr.getArtifact());
    }

    @Test
    public void should_not_migrate_page_when_page_not_compatible() {
        Page page = PageBuilder.aPage().withId("myPage").withModelVersion("3.0").build();
        when(pageRepository.get("myPage")).thenReturn(page);

        pageService.get("myPage");

        verify(pageMigrationApplyer, never()).migrate(page);
        verify(pageRepository, never()).updateLastUpdateAndSave(any(Page.class));
    }

    @Test
    public void should_not_migrate_page_when_dependencies_not_compatible() {
        Page page = PageBuilder.aPage().withId("myPage").withModelVersion("2.0").build();
        when(pageRepository.get("myPage")).thenReturn(page);

        pageService.get("myPage");

        verify(pageMigrationApplyer, never()).migrate(page);
        verify(pageRepository, never()).updateLastUpdateAndSave(any(Page.class));
    }

    @Test
    public void should_migrate_page_when_no_artifact_version_is_declared() {
        Page page = PageBuilder.aPage().withId("myPage").build();
        Page migratedPage = PageBuilder.aPage().withId("myPage").withModelVersion("2.0").build();
        when(pageRepository.get("myPage")).thenReturn(page);
        MigrationResult mr = new MigrationResult(migratedPage, asList(new MigrationStepReport(MigrationStatus.SUCCESS)));
        when(pageMigrationApplyer.migrate(page)).thenReturn(mr);
        doReturn(new MigrationStatusReport(true, true)).when(pageService).getStatus(any());

        pageService.get("myPage");

        verify(pageMigrationApplyer).migrate(page);
        verify(pageRepository).updateLastUpdateAndSave(any(Page.class));
    }

    // FROM CONTROLLER

    private Asset aPageAsset() {
        return anAsset().withName("myJs.js").withType(JAVASCRIPT).build();
    }

    private Asset aWidgetAsset() {
        return anAsset().withName("myCss.css").withType(AssetType.CSS).withScope(AssetScope.WIDGET).withComponentId("widget-id").build();
    }

    private Page mockPageOfId(String id) {
        Page page = aPage().withId(id).build();
        when(pageRepository.get(id)).thenReturn(page);
        return page;
    }

    @Test
    public void should_list_pages() throws Exception {
        Page page = new Page();
        final String pageId = "id";
        page.setId(pageId);
        final String pageName = "name";
        page.setName(pageName);

        final Instant pageLastUpdate = Instant.parse("2015-02-02T00:00:00.000Z");
        page.setLastUpdate(pageLastUpdate);

        final List<Page> pageList = singletonList(page);
        when(pageRepository.getAll()).thenReturn(pageList);

        // When
        final List<Page> all = pageService.getAll();

        assertThat(all)
                .hasSize(pageList.size())
                .contains(page);

        final Page firstPage = all.get(0);
        assertThat(firstPage.getId()).isEqualTo(pageId);
        assertThat(firstPage.getName()).isEqualTo(pageName);
        assertThat(firstPage.getLastUpdate()).isEqualTo(pageLastUpdate);
        assertThat(firstPage.getStatus()).isEqualTo(defaultStatusReport);
    }


    @Test
    public void should_create_a_page_from_a_Page() throws Exception {
        Page pageToBeSaved = aPage().withId("my-page").build();
        List<Element> emptyRow = emptyList();
        List<List<Element>> rows = singletonList(emptyRow);
        pageToBeSaved.setRows(rows);
        final String name = "test";
        pageToBeSaved.setName(name);
        when(pageRepository.updateLastUpdateAndSave(pageToBeSaved)).thenReturn(pageToBeSaved);
        when(pageRepository.getNextAvailableId(name)).thenReturn(name);

        // When
        final Page savedPage = pageService.create(pageToBeSaved);

        // Then
        assertThat(savedPage.getId()).isEqualTo(name);
        verify(pageRepository).updateLastUpdateAndSave(pageToBeSaved);
        verify(pageAssetService).loadDefaultAssets(pageToBeSaved);
    }

    @Test
    public void should_duplicate_a_page_from_a_Page() throws Exception {
        Asset pageAsset = aPageAsset();
        Asset widgetAsset = aWidgetAsset();

        final String name = "test";
        Page pageToBeSaved = aPage().withId("my-page").withName(name).withAsset(pageAsset, widgetAsset).build();

        final String pageSourceId = "my-page-source";
        when(pageRepository.getNextAvailableId(name)).thenReturn(name);

        // When
        final Page savedPage = pageService.createFrom(pageSourceId, pageToBeSaved);

        // Then
        assertThat(savedPage.getId()).isEqualTo(name);
        verify(pageRepository).updateLastUpdateAndSave(pageToBeSaved);
        assertThat(savedPage.getName()).isEqualTo(pageToBeSaved.getName());
        assertThat(savedPage.getAssets()).containsOnly(pageAsset);
        verify(pageAssetService).duplicateAsset(any(), any(), eq(pageSourceId), anyString());
    }


    @Test
    public void should_save_a_page() throws Exception {
        final String pageId = "my-page";
        Page pageToBeSaved = mockPageOfId(pageId);

        final Page savedPage = pageService.save(pageId, pageToBeSaved);

        verify(pageRepository).updateLastUpdateAndSave(pageToBeSaved);
    }

    @Test
    public void should_save_a_page_with_fragment() throws Exception {

        Page pageToBeSaved = aPageWithFragmentElement();
        final String name = pageToBeSaved.getName();
        pageToBeSaved.setId(name);

        when(pageRepository.get(name)).thenThrow(new NotFoundException());
        when(pageRepository.getNextAvailableId(name)).thenReturn(name);

        // When
        final Page savedPage = pageService.save(pageToBeSaved.getId(), pageToBeSaved);

        verify(pageRepository).updateLastUpdateAndSave(pageToBeSaved);
        assertThat(savedPage.getRows()).isNotEmpty();
    }


    @Test
    public void should_save_a_page_renaming_it() throws Exception {

        final String myPageName = "my-page";
        Page existingPage = aPage().withId(myPageName).withName(myPageName).build();
        existingPage.addAsset(AssetBuilder.aFilledAsset(existingPage));
        when(pageRepository.get(myPageName)).thenReturn(existingPage);

        final String newPageName = "page-new-name";
        Page pageToBeSaved = aPage().withName(newPageName).build();
        pageToBeSaved.addAsset(AssetBuilder.aFilledAsset(existingPage));
        when(pageRepository.getNextAvailableId(newPageName)).thenReturn(newPageName);


        // When
        final Page savedPage = pageService.save(myPageName, pageToBeSaved);

        // Then
        assertThat(savedPage.getId()).isEqualTo(newPageName);
        verify(pageRepository).updateLastUpdateAndSave(aPage().withId(newPageName).withName(newPageName).build());
        verify(pageRepository).delete("my-page");
        verify(pageAssetService).duplicateAsset(pageRepository.resolvePath(myPageName), pageRepository.resolvePath(myPageName), myPageName, newPageName);
    }

    @Test
    public void should_not_save_widget_assets_while_saving_a_page() throws Exception {
        Page pageToBeSaved = aPage().withId("my-page")
                .withAsset(aPageAsset())
                .build();
        when(pageRepository.get("my-page")).thenReturn(pageToBeSaved);

        Page expectedPage = aPage().withId("my-page")
                .withAsset(aPageAsset())
                .build();

        final Page savedPage = pageService.save(pageToBeSaved.getId(), pageToBeSaved);

        verify(pageRepository).updateLastUpdateAndSave(expectedPage);
        verify(pageAssetService,never()).duplicateAsset(any(),any(),anyString(),anyString());
        assertThat(savedPage).isEqualTo(expectedPage);
    }


    @Test
    public void should_retrieve_a_page_representation_by_its_id() throws Exception {
        final String pageId = "my-page";
        Page expectedPage = aFilledPage(pageId);
        expectedPage.setStatus(new MigrationStatusReport(true, true));
        when(pageRepository.get(pageId)).thenReturn(expectedPage);

        final Page savedPage = pageService.get(pageId);

        assertThat(savedPage).isEqualTo(expectedPage);
    }

    @Test
    public void should_respond_ex_not_found_if_page_is_not_existing() throws Exception {
        final String pageId = "my-page";
        when(pageRepository.get(pageId)).thenThrow(new NotFoundException("page not found"));

        assertThatThrownBy(() ->
                pageService.get(pageId)
        ).isInstanceOf(NotFoundException.class);
    }

    @Test
    public void should_respond_422_on_save_when_page_is_incompatible() throws Exception {
        final String pageId = "my-page";
        Page pageToBeSaved = mockPageOfId(pageId);
        doReturn(new MigrationStatusReport(false, false)).when(pageService).getStatus(any());

        when(pageRepository.get(pageId)).thenReturn(pageToBeSaved);

        //When
        assertThatThrownBy(() -> pageService.save(pageId, pageToBeSaved)).isInstanceOf(IncompatibleException.class);

        verify(pageRepository, never()).updateLastUpdateAndSave(pageToBeSaved);
    }

    @Test
    public void should_respond_404_not_found_when_delete_inexisting_page() throws Exception {

        final String pageId = "my-page";
        doThrow(new NotFoundException("page not found")).when(pageRepository).delete(pageId);

        assertThatThrownBy(() -> pageService.delete(pageId)).isInstanceOf(NotFoundException.class);

    }

    @Test
    public void should_rename_a_page() throws Exception {
        String newName = "my-page-new-name";
        final String pageId = "my-page";
        Page pageToBeUpdated = aPage().withId(pageId).withName("page-name").build();
        when(pageRepository.get(pageId)).thenReturn(pageToBeUpdated);
        when(pageRepository.getNextAvailableId(newName)).thenReturn(newName);


        //When
        final Page savedPage = pageService.rename(pageId, newName);

        verify(pageRepository).getNextAvailableId(newName);
        verify(pageRepository).updateLastUpdateAndSave(pageToBeUpdated);
        assertThat(pageToBeUpdated.getName()).isEqualTo(newName);
        assertThat(pageToBeUpdated.getId()).isEqualTo(newName);
        assertThat(savedPage.getId()).isEqualTo(newName);
    }

    @Test
    public void should_not_rename_a_page_if_name_is_same() throws Exception {
        String name = "page-name";
        final String pageId = "my-page";

        Page pageToBeUpdated = aPage().withId(pageId).withName(name).build();
        when(pageRepository.get(pageId)).thenReturn(pageToBeUpdated);

        // When
        final Page page = pageService.rename(pageId, name);

        verify(pageRepository, never()).updateLastUpdateAndSave(any(Page.class));
    }

    @Test
    public void should_keep_assets_when_page_is_renamed() throws Exception {
        String newName = "my-page-new-name";

        final String pageId = "my-page";
        Page pageToBeUpdated = aPage().withId(pageId).withName("page-name").build();
        pageToBeUpdated.addAsset(AssetBuilder.aFilledAsset(pageToBeUpdated));

        when(pageRepository.get(pageId)).thenReturn(pageToBeUpdated);
        when(pageRepository.getNextAvailableId(newName)).thenReturn(newName);

        //When
        final Page page = pageService.rename(pageId, newName);

        //Then
        verify(pageAssetService).duplicateAsset(pageRepository.resolvePath(pageId), pageRepository.resolvePath(pageId), pageId, newName);

        assertThat(page.getName()).isEqualTo(newName);
        assertThat(page.getId()).isEqualTo(newName);
        assertThat(page.getAssets()).isNotEmpty();
    }


    @Test
    public void should_respond_404_not_found_if_page_is_not_existing_when_renaming() throws Exception {
        final String pageId = "my-page";
        when(pageRepository.get(pageId)).thenThrow(new NotFoundException("page not found"));

        //When
        assertThatThrownBy(() -> pageService.rename(pageId, "hello")).isInstanceOf(NotFoundException.class);
    }

    @Test
    public void should_respond_500_internal_error_if_error_occurs_while_renaming_a_page() throws Exception {

        final String pageId = "my-page";
        when(pageRepository.get(pageId)).thenReturn(aPage().withId(pageId).withName(pageId).build());
        when(pageRepository.updateLastUpdateAndSave(any())).thenThrow(new RepositoryException("exception occurs", new Exception()));

        //When
        assertThatThrownBy(() -> pageService.rename(pageId, "hello")).isInstanceOf(RepositoryException.class);
    }

    @Test
    public void should_upload_a_local_asset() throws Exception {
        //We construct a mockfile (the first arg is the name of the property expected in the controller
        final String fileName = "myfile.js";
        final byte[] fileContent = "foo".getBytes();
        final String pageId = "my-page";
        Page page = mockPageOfId(pageId);
        Asset expectedAsset = anAsset().withId("assetId").active()
                .withName(fileName).withOrder(2).withScope(PAGE)
                .withType(JAVASCRIPT).build();
        when(pageAssetService.save(eq(page), any(), eq(fileContent))).thenReturn(expectedAsset);

        final Asset asset = pageService.saveOrUpdateAsset(pageId, JAVASCRIPT, fileName, fileContent);

        assertThat(asset).isEqualTo(expectedAsset);
    }


    private MalformedJsonException aMalformedJsonException(byte[] bytes, int errorLine, int errorColumn) {
        return new MalformedJsonException(new FakeJsonProcessingException("Error while checking json", bytes, errorLine, errorColumn));
    }

    @Test
    public void should_save_an_external_asset() throws Exception {
        Page page = mockPageOfId("my-page");
        Asset expectedAsset = anAsset().withId("assetId").active().withName("myfile.js").withOrder(2).withScope(PAGE)
                .withType(JAVASCRIPT).build();
        when(pageAssetService.save(page, expectedAsset)).thenReturn(expectedAsset);

        final Asset asset = pageService.saveAsset(page.getId(), expectedAsset);

        assertThat(asset).isEqualTo(expectedAsset);

        verify(pageAssetService).save(page, expectedAsset);
    }

    @Test
    public void should_not_save_an_external_asset_when_upload_send_an_error() throws Exception {
        Page page = mockPageOfId("my-page");
        Asset asset = anAsset().build();
        doThrow(IllegalArgumentException.class).when(pageAssetService).save(page, asset);

        assertThatThrownBy(() -> pageService.saveAsset(page.getId(), asset)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_list_page_assets() throws Exception {
        Page page = mockPageOfId("my-page");
        Set<Asset> assets = Set.of(
                anAsset().withName("myCss.css").withType(AssetType.CSS).withScope(AssetScope.WIDGET).withComponentId("widget-id").build(),
                anAsset().withName("myJs.js").withType(JAVASCRIPT).withScope(AssetScope.PAGE).build(),
                anAsset().withName("https://mycdn.com/myExternalJs.js").withScope(AssetScope.PAGE).withType(JAVASCRIPT).build()
        );
        when(assetVisitor.visit(page)).thenReturn(assets);

        //When
        final Set<Asset> pageAssets = pageService.listAsset(page);

        assertThat(pageAssets).isEqualTo(assets);
    }

    @Test
    public void should_list_page_assets_while_getting_a_page() throws Exception {
        Page page = mockPageOfId("my-page");
        Set<Asset> assets = Set.of(
                anAsset().withName("myCss.css").withType(AssetType.CSS).withScope(AssetScope.WIDGET).withComponentId("widget-id").build(),
                anAsset().withName("myJs.js").withType(JAVASCRIPT).withScope(AssetScope.PAGE).build(),
                anAsset().withName("https://mycdn.com/myExternalJs.js").withScope(AssetScope.PAGE).withType(JAVASCRIPT).build()
        );
        when(assetVisitor.visit(page)).thenReturn(assets);

        //When
        final Page pageAgain = pageService.getWithAsset(page.getId());

        assertThat(pageAgain.getAssets()).isEqualTo(assets);
    }


    @Test
    public void should_increment_an_asset() throws Exception {
        Page page = mockPageOfId("my-page");
        Asset asset = anAsset().withId("UIID").withComponentId("my-page").withOrder(3).build();

        pageService.changeAssetOrder(page.getId(), asset.getId(), INCREMENT);

        verify(pageAssetService).changeAssetOrderInComponent(page, asset.getId(), INCREMENT);
    }

    @Test
    public void should_decrement_an_asset() throws Exception {
        Page page = mockPageOfId("my-page");
        Asset asset = anAsset().withId("UIID").withComponentId("my-page").withOrder(3).build();

        pageService.changeAssetOrder(page.getId(), asset.getId(), DECREMENT);

        verify(pageAssetService).changeAssetOrderInComponent(page, asset.getId(), DECREMENT);
    }

    @Test
    public void should_delete_an_asset() throws Exception {
        final String pageId = "my-page";
        final String assetId = "UIID";
        Page page = mockPageOfId(pageId);

        pageService.deleteAsset(pageId, assetId);

        verify(pageAssetService).delete(page, "UIID");
    }

    @Test
    public void should_inactive_an_asset() throws Exception {
        Page page = mockPageOfId("my-page");
        Asset asset = anAsset().withComponentId(page.getId()).withOrder(3).build();

        pageService.changeAssetStateInPreviewable(page.getId(), "UIID", false);


        verify(pageAssetService).changeAssetStateInPreviewable(page, "UIID", false);
    }

    @Test
    public void should_mark_a_page_as_favorite() throws Exception {

        pageService.markAsFavorite("my-page", true);
        verify(pageRepository).markAsFavorite("my-page");
    }

    @Test
    public void should_unmark_a_page_as_favorite() throws Exception {

        pageService.markAsFavorite("my-page", false);
        verify(pageRepository).unmarkAsFavorite("my-page");
    }

    @Test
    public void should_load_page_asset_on_disk_with_content_type_text() throws Exception {
        pageService.findAssetPath("id", "fileName", "js");
        verify(pageAssetService).findAssetPath("id", "fileName", "js");
    }

    private Variable anApiVariable(String value) {
        return new Variable(URL, value);
    }

    private Page setUpPageForResourcesTests() {
        Page page = mockPageOfId("myPage");
        when(componentVisitor.visit(page)).thenReturn(Collections.<Component>emptyList());
        return page;
    }

    private Page setUpPageWithFragmentForResourcesTests() {
        Fragment fragment = aFragment().withId("myFragment").build();
        HashMap<String, Variable> variables = new HashMap<>();
        variables.put("fragAPI", anApiVariable("../API/bpm/process/1"));
        variables.put("fragAPIExt", anApiVariable("../API/extension/user/4"));
        fragment.setVariables(variables);
        when(fragmentService.get("myFragment")).thenReturn(fragment);

        FragmentElement fragmentElement = new FragmentElement();
        fragmentElement.setId("myFragment");
        fragmentElement.setDimension(Map.of("md", 8));

        Page page = aPage().withId("myPage").with(fragmentElement).build();
        page.setVariables(singletonMap("foo", anApiVariable("../API/bpm/userTask?filter=mine")));
        when(componentVisitor.visit(page)).thenReturn(Collections.<Component>emptyList());
        TreeSet<String> fragmentIds = new TreeSet<String>();
        fragmentIds.add("myFragment");
        when(fragmentIdVisitor.visit(page)).thenReturn(fragmentIds);
        return page;
    }

    @Test
    public void should_add_bonita_resources_found_in_pages_widgets() throws Exception {
        Page page = setUpPageForResourcesTests();

        page.setVariables(singletonMap("foo", anApiVariable("../API/bpm/userTask?filter=mine")));

        final List<String> resources = pageService.getResources(page);
        String properties = resources.toString();

        assertThat(properties).contains("[GET|bpm/userTask]");
    }

    @Test
    public void should_add_bonita_resources_found_in_fragments() throws Exception {
        Page page = setUpPageWithFragmentForResourcesTests();

        final List<String> resources = pageService.getResources(page);
        String properties = resources.toString();

        assertThat(properties).contains("[GET|bpm/userTask, GET|bpm/process, GET|extension/user/4]");
    }

    @Test
    public void should_add_bonita_api_extensions_resources_found_in_pages_widgets() throws Exception {
        Page page = setUpPageForResourcesTests();
        List<String> authRules = new ArrayList<>();
        authRules.add("POST|bpm/process");

        HashMap<String, Variable> variables = new HashMap<>();
        variables.put("foo", anApiVariable("../API/extension/CA31/SQLToObject?filter=mine"));
        // Not supported platform side. Prefer use queryParam like ?id=4
        variables.put("bar", anApiVariable("../API/extension/user/4"));
        variables.put("aa", anApiVariable("../API/extension/group/list"));
        variables.put("session", anApiVariable("../API/extension/user/group/unusedid"));
        variables.put("ab", anApiVariable("http://localhost:8080/bonita/portal/API/extension/vehicule/voiture/roue?p=0&c=10&f=case_id={{caseId}}"));
        variables.put("user", anApiVariable("../API/identity/user/{{aaa}}/context"));
        variables.put("task", anApiVariable("../API/bpm/task/1/context"));
        variables.put("case", anApiVariable("../API/bpm/case{{dynamicQueries(true,0)}}"));
        variables.put("custom", anApiVariable("../API/extension/case{{dynamicQueries}}"));
        page.setVariables(variables);

        String properties = pageService.getResources(page).toString();

        assertThat(properties).contains("[GET|bpm/task, GET|identity/user, GET|bpm/case, GET|extension/group/list, GET|extension/vehicule/voiture/roue, GET|extension/user/4, GET|extension/user/group/unusedid, GET|extension/CA31/SQLToObject, GET|extension/case]");
    }

    @Test
    public void should_add_bonita_api_extensions_resources_found_in_page_data_table_properties() throws Exception {
        Page page = setUpPageForResourcesTests();
        Component dataTableComponent = aComponent()
                .withPropertyValue("apiUrl", "constant", "../API/extension/car")
                .build();
        when(componentVisitor.visit(page)).thenReturn(singleton(dataTableComponent));

        String properties = pageService.getResources(page).toString();

        assertThat(properties).contains("[GET|extension/car]");
    }

    @Test
    public void should_add_bonita_api_extensions_resources_found_in_page_button_with_DELETE_action() throws Exception {
        Page page = setUpPageForResourcesTests();
        Component dataTableComponent = aComponent()
                .withPropertyValue("action", "constant", "DELETE")
                .withPropertyValue("url", ParameterType.INTERPOLATION.getValue(), "../API/bpm/document/1")
                .build();
        when(componentVisitor.visit(page)).thenReturn(singleton(dataTableComponent));

        String properties = pageService.getResources(page).toString();

        assertThat(properties).contains("[DELETE|bpm/document]");
    }

    @Test
    public void should_add_bonita_api_extensions_resources_found_in_page_button_with_POST_action() throws Exception {
        Page page = setUpPageForResourcesTests();
        Component dataTableComponent = aComponent()
                .withPropertyValue("action", "constant", "POST")
                .withPropertyValue("url", ParameterType.INTERPOLATION.getValue(), "../API/extension/user")
                .build();
        when(componentVisitor.visit(page)).thenReturn(singleton(dataTableComponent));

        String properties = pageService.getResources(page).toString();

        assertThat(properties).contains("[POST|extension/user]");
    }

    @Test
    public void should_add_bonita_api_extensions_resources_found_in_page_fileUpload_widget() throws Exception {
        Page page = setUpPageForResourcesTests();
        Component fileUploadComponent = aComponent()
                .withWidgetId("pbUpload")
                .withPropertyValue("url", ParameterType.CONSTANT.getValue(), "../API/extension/upload")
                .build();
        when(componentVisitor.visit(page)).thenReturn(singleton(fileUploadComponent));

        String properties = pageService.getResources(page).toString();

        assertThat(properties).contains("[POST|extension/upload]");
    }

    @Test
    public void should_add_start_process_resource_if_a_start_process_submit_is_contained_in_the_page() throws Exception {
        Page page = setUpPageForResourcesTests();
        when(componentVisitor.visit(page)).thenReturn(
                singleton(aComponent()
                        .withPropertyValue("action", "constant", "Start process")
                        .build())
        );

        String properties = pageService.getResources(page).toString();

        assertThat(properties).contains("[POST|bpm/process]");
    }

    @Test
    public void should_show_the_correct_information_for_variables() throws Exception {
        Path expectedFilePath = Paths.get(getClass().getResource("/page-with-variables/").toURI()).resolve("page-with-variables.json");
        String expectedFileString = String.join("", readAllLines(expectedFilePath));

        Map<String, Variable> variables = new HashMap<>();
        variables.put("constantVar", new Variable(DataType.CONSTANT, "constantVariableValue"));
        variables.put("jsonVar", new Variable(DataType.JSON, "{\"var1\":1, \"var2\":2, \"var3\":\"value3\"}"));
        variables.put("jsVar", new Variable(DataType.EXPRESSION, "var variable = \"hello\"; return variable;"));
        Page page = new Page();
        page.setId("page-with-variables");
        page.setVariables(variables);
        page.setDesignerVersion("1.10.6");
        page.setName("page");
        page.setLastUpdate(Instant.ofEpochMilli(1514989634397L));
        page.setRows(new ArrayList<>());
        page.setStatus(new MigrationStatusReport());

        when(pageRepository.get("id")).thenReturn(page);

        final Page myPage = pageService.get("id");
        assertThat(myPage.getVariables()).isEqualTo(variables);
    }

    @Test
    public void should_add_submit_task_resource_if_a_start_submit_task_is_contained_in_the_page() throws Exception {
        final Page page = setUpPageForResourcesTests();
        when(componentVisitor.visit(page)).thenReturn(singleton(aComponent()
                .withPropertyValue("action", "constant", "Submit task")
                .build())
        );

        String properties = pageService.getResources(page).toString();

        assertThat(properties).contains("[POST|bpm/userTask]");
    }

    @Test
    public void should_combined_start_process_submit_task_and_bonita_resources() throws Exception {
        final Page page = setUpPageForResourcesTests();
        when(componentVisitor.visit(page))
                .thenReturn(asList(
                        aComponent()
                                .withPropertyValue("action", "constant", "Start process")
                                .build(),
                        aComponent()
                                .withPropertyValue("action", "constant", "Submit task")
                                .build()));
        page.setVariables(singletonMap("foo", anApiVariable("/bonita/API/bpm/userTask")));

        String properties = pageService.getResources(page).toString();

        assertThat(properties).contains("[GET|bpm/userTask, POST|bpm/process, POST|bpm/userTask]");
    }

    @Test
    public void should_upload_newfile_and_save_new_asset() throws Exception {
        Page page = mockPageOfId("aPage");
        byte[] fileContent = "function(){}".getBytes();
        Asset expectedAsset = anAsset()
                .withName("originalFileName.js")
                .withType(AssetType.JAVASCRIPT)
                .withOrder(1).build();
        when(pageAssetService.save(eq(page), any(), eq(fileContent))).thenReturn(expectedAsset);

        Asset asset = pageService.saveOrUpdateAsset(page.getId(), AssetType.JAVASCRIPT, "originalFileName.js", fileContent);

        verify(pageAssetService).save(page, asset, fileContent);

        assertThat(asset).isEqualTo(expectedAsset);
    }

    @Test
    public void should_upload_a_json_asset() throws Exception {
        Page page = mockPageOfId("page-id");
        byte[] fileContent = "{ \"some\": \"json\" }".getBytes();
        Asset expectedAsset = anAsset()
                .withName("asset.json")
                .withType(AssetType.JSON)
                .withOrder(1).build();
        when(pageAssetService.save(eq(page), any(), eq(fileContent))).thenReturn(expectedAsset);

        Asset asset = pageService.saveOrUpdateAsset(page.getId(), AssetType.JSON, "asset.json", fileContent);

        verify(pageAssetService).save(page, asset, fileContent);
        assertThat(asset).isEqualTo(expectedAsset);   }

    @Test
    public void should_return_error_when_uploading_with_error_onsave() throws Exception {
        Page page = mockPageOfId("id");

        final String message = "Error while saving internal asset";
        when(pageAssetService.save(any(),any(),any())).thenThrow(new RepositoryException(message, new IllegalArgumentException()));

        final Throwable exception = catchThrowable(() -> {
            pageService.saveOrUpdateAsset(page.getId(), JAVASCRIPT, page.getId(), "0".getBytes(UTF_8));
        });
        assertThat(exception).isInstanceOf(RepositoryException.class);
        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    public void should_upload_existing_file() throws Exception {
        Asset existingAsset = anAsset().withId("UIID").withName("asset.js").build();
        Page page = aPage().withId("page-id").withName("my-page").withAsset(existingAsset).build();
        when(pageRepository.get(page.getId())).thenReturn(page);
        final byte[] bytes = "function(){}".getBytes();
        when(pageAssetService.save(eq(page), any(), eq(bytes))).thenReturn(existingAsset);


        Asset asset = pageService.saveOrUpdateAsset(page.getId(), JAVASCRIPT, "asset.js", bytes);

        verify(pageAssetService).save(page, page.getAssets().iterator().next(), bytes);
        assertThat(asset.getId()).isEqualTo(existingAsset.getId());
    }

}
