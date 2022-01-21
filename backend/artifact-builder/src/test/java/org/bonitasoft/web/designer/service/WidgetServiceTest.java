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

import org.bonitasoft.web.designer.builder.PageBuilder;
import org.bonitasoft.web.designer.builder.PropertyBuilder;
import org.bonitasoft.web.designer.config.UiDesignerProperties;
import org.bonitasoft.web.designer.controller.MigrationStatusReport;
import org.bonitasoft.web.designer.controller.asset.AssetService;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.asset.AssetType;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.model.migrationReport.MigrationResult;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStatus;
import org.bonitasoft.web.designer.model.migrationReport.MigrationStepReport;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.widget.Property;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.FragmentRepository;
import org.bonitasoft.web.designer.repository.PageRepository;
import org.bonitasoft.web.designer.repository.WidgetRepository;
import org.bonitasoft.web.designer.repository.exception.InUseException;
import org.bonitasoft.web.designer.repository.exception.NotAllowedException;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;
import org.bonitasoft.web.designer.visitor.AssetVisitor;
import org.bonitasoft.web.designer.visitor.WidgetIdVisitor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.bonitasoft.web.designer.builder.AssetBuilder.anAsset;
import static org.bonitasoft.web.designer.builder.ComponentBuilder.aComponent;
import static org.bonitasoft.web.designer.builder.FragmentBuilder.aFragment;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.builder.PropertyBuilder.aProperty;
import static org.bonitasoft.web.designer.builder.WidgetBuilder.aWidget;
import static org.bonitasoft.web.designer.controller.asset.AssetService.OrderType.DECREMENT;
import static org.bonitasoft.web.designer.controller.asset.AssetService.OrderType.INCREMENT;
import static org.bonitasoft.web.designer.model.widget.BondType.CONSTANT;
import static org.bonitasoft.web.designer.model.widget.BondType.INTERPOLATION;
import static java.time.Instant.parse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class WidgetServiceTest {

    private static final String CURRENT_MODEL_VERSION = "2.0";

    @Mock
    private WidgetRepository widgetRepository;

    @Mock
    private PageRepository pageRepository;

    @Mock
    private FragmentRepository fragmentRepository;

    @Mock
    private BondsTypesFixer bondsTypesFixer;

    @Mock
    private WidgetMigrationApplyer widgetMigrationApplyer;

    @Mock
    private WidgetIdVisitor widgetIdVisitor;

    @Mock
    private AssetService<Widget> widgetAssetService;

    @InjectMocks
    private DefaultWidgetService widgetService;
    private MigrationStatusReport migrationStatusReport;

    private AssetVisitor assetVisitor;

    UiDesignerProperties uiDesignerProperties;

    @Before
    public void setUp() throws Exception {
        assetVisitor = new AssetVisitor(widgetRepository, fragmentRepository);

        uiDesignerProperties = new UiDesignerProperties("1.13.0", CURRENT_MODEL_VERSION);
        uiDesignerProperties.getWorkspace().getWidgets().setDir(Paths.get("Widget"));

        widgetService = spy(new DefaultWidgetService(
                widgetRepository,
                pageRepository,
                fragmentRepository,
                singletonList(bondsTypesFixer),
                widgetMigrationApplyer,
                widgetIdVisitor,
                assetVisitor,
                uiDesignerProperties,
                widgetAssetService
        ));
        migrationStatusReport = new MigrationStatusReport(true, false);
        doReturn(migrationStatusReport).when(widgetService).getStatus(any());
    }

    @Test
    public void should_serve_all_widgets_in_repository() throws Exception {
        //Given
        Widget input = aWidget().withId("input").build();
        Widget label = aWidget().withId("label").build();
        List<Widget> expectedWidgetList = asList(input, label);
        when(widgetRepository.getAll()).thenReturn(expectedWidgetList);

        //When
        List<Widget> widgets = widgetService.getAll();

        //Then
        assertThat(widgets).hasSameSizeAs(expectedWidgetList);
        assertThat(widgets).contains(input);
        assertThat(widgets).contains(label);
    }

    @Test
    public void should_serve_empty_list_if_widget_repository_is_empty() throws Exception {
        when(widgetRepository.getAll()).thenReturn(new ArrayList<Widget>());

        List<Widget> widgets = widgetService.getAll();

        assertThat(widgets).isEmpty();

    }

    @Test
    public void should_throw_repo_exception_if_an_error_occurs_while_getting_widgets() throws Exception {
        when(widgetRepository.getAll()).thenThrow(new RepositoryException("error occurs", new Exception()));

        assertThatThrownBy(() -> widgetService.getAll()).isInstanceOf(RepositoryException.class);
    }

    @Test
    public void should_get_a_widget_by_its_id() throws Exception {
        String widgetId = "input";
        Widget input = aWidget().withId(widgetId).build();

        when(widgetRepository.get(widgetId)).thenReturn(input);
        when(widgetService.migrate(input)).thenReturn(input);

        Widget widget = widgetService.get(widgetId);

        assertThat(widget).isEqualTo(input);
        assertThat(widget.getAssets()).hasSize(0);
    }

    @Test
    public void should_get_a_widget_with_asset_by_its_id() throws Exception {
        String widgetId = "input";
        Widget input = aWidget().withId(widgetId).assets(anAsset().withName("myScopeWidgetAsset").withType(AssetType.CSS)).build();

        when(widgetRepository.get(widgetId)).thenReturn(input);
        when(widgetService.migrate(input)).thenReturn(input);

        Widget widget = widgetService.getWithAsset(widgetId);

        assertThat(widget).isEqualTo(input);
        assertThat(widget.getAssets()).hasSize(1);
        assertThat(widget.getAssets().stream().findFirst().get().getScope()).isEqualTo("widget");
    }

    @Test
    public void should_throw_NotFoundException_when_getting_an_unexisting_widget() throws Exception {
        when(widgetRepository.get("notExistingWidget")).thenThrow(new NotFoundException("not found"));

        assertThatThrownBy(() -> widgetService.getWithAsset("notExistingWidget")).isInstanceOf(NotFoundException.class);
    }

    @Test
    public void should_save_a_widget() throws Exception {
        Widget customLabel = aWidget().withId("customLabel").custom().build();

        widgetService.save("customLabel", customLabel);

        verify(widgetRepository).updateLastUpdateAndSave(customLabel);
    }

    @Test
    public void should_not_allow_to_save_a_pb_widget() throws Exception {
        Widget pbWidget = aWidget().custom().build();

        assertThatThrownBy(() -> widgetService.save("pbLabel", pbWidget)).isInstanceOf(NotAllowedException.class);
    }

    @Test
    public void should_not_allow_to_save_a_not_custom_widget() throws Exception {
        Widget pbWidget = aWidget().withId("input").build();

        assertThatThrownBy(() -> widgetService.save("customLabel", pbWidget)).isInstanceOf(NotAllowedException.class);
    }

    @Test
    public void should_throw_RepositoryException_if_an_error_occurs_while_saving_a_widget() throws Exception {
        Widget customLabel = aWidget().withId("customLabel").custom().build();
        doThrow(new RepositoryException("error occurs", new Exception())).when(widgetRepository).updateLastUpdateAndSave(customLabel);

        assertThatThrownBy(() -> widgetService.save("customLabel", customLabel)).isInstanceOf(RepositoryException.class);
    }

    @Test
    public void should_create_a_new_widget() throws Exception {
        Widget customLabel = aWidget().withName("label").custom().build();
        when(widgetRepository.create(customLabel)).thenReturn(customLabel);

        widgetService.create(customLabel);

        verify(widgetRepository).create(notNull());
    }

    @Test
    public void should_duplicate_a_widget_from_a_widget() throws Exception {
        Widget customLabel = aWidget().withName("label").assets(anAsset().withName("myfile.js")).custom().build();
        String sourceWidgetId = "my-widget-source";
        when(widgetRepository.create(customLabel)).thenReturn(customLabel);
        Path sourceWidgetPath = Paths.get("my-widget-source");
        when(widgetRepository.resolvePath(sourceWidgetId)).thenReturn(sourceWidgetPath);

        Widget savedWidget = widgetService.createFrom(sourceWidgetId, customLabel);

        verify(widgetRepository).create(customLabel);
        verify(widgetAssetService).duplicateAsset(uiDesignerProperties.getWorkspace().getWidgets().getDir(), sourceWidgetPath, sourceWidgetId, customLabel.getId());
        assertThat(savedWidget).isEqualTo(customLabel);
    }

    @Test
    public void should_not_allow_to_create_a_widget_with_an_empty_name() throws Exception {
        Widget customLabel = aWidget().withName("").custom().build();
        when(widgetRepository.create(customLabel)).thenThrow(new IllegalArgumentException());

        assertThatThrownBy(() -> widgetService.create(customLabel)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_not_allow_to_create_a_widget_with_an_existing_name() throws Exception {
        Widget customLabel = aWidget().withName("alreadyExistingName").build();
        when(widgetRepository.create(customLabel)).thenThrow(new NotAllowedException("already existing name"));

        assertThatThrownBy(() -> widgetService.create(customLabel)).isInstanceOf(NotAllowedException.class);
    }

    @Test
    public void should_delete_a_widget() throws Exception {
        Widget customLabel = aWidget().custom().withId("customLabel").build();
        when(widgetRepository.get("customLabel")).thenReturn(customLabel);
        when(fragmentRepository.getArtifactsUsingWidget(customLabel.getId())).thenReturn(emptyList());
        when(pageRepository.getArtifactsUsingWidget(customLabel.getId())).thenReturn(emptyList());
        widgetService.delete("customLabel");
        verify(widgetRepository).delete("customLabel");
    }

    @Test
    public void should_not_allow_to_delete_a_pb_widget() throws Exception {
        when(widgetRepository.get("pbWidget")).thenReturn(aWidget().withId("pbWidget").build());
        assertThatThrownBy(() -> widgetService.delete("pbWidget")).isInstanceOf(NotAllowedException.class);
    }

    @Test
    public void should_throw_NotFoundException_if_trying_to_delete_an_unknown_widget() throws Exception {
        doThrow(new NotFoundException("not found")).when(widgetRepository).get("customLabel");
        assertThatThrownBy(() -> widgetService.delete("customLabel")).isInstanceOf(NotFoundException.class);
    }

    @Test
    public void should_not_allow_to_delete_a_custom_widget_used_in_a_page() throws Exception {
        when(widgetRepository.get("customLabel")).thenReturn(aWidget().custom().withId("customLabel").build());
        when(pageRepository.getComponentName()).thenReturn("page");
        when(pageRepository.getArtifactsUsingWidget("customLabel")).thenReturn(asList(aPage().withName("person").build()));
        when(fragmentRepository.getComponentName()).thenReturn("fragment");
        when(fragmentRepository.getArtifactsUsingWidget("customLabel"))
                .thenReturn(asList(aFragment().withName("personFragment1").build(),
                        aFragment().withName("personFragment2").build()));

        assertThatThrownBy(() -> widgetService.delete("customLabel"))
                .isInstanceOf(InUseException.class)
                .hasMessage("The widget cannot be deleted because it is used in 2 fragments, <personFragment1>, <personFragment2> 1 page, <person>");
    }

    @Test
    public void should_throw_not_found_if_custom_widget_is_not_existing_when_renaming() throws Exception {
        Property requestProperty = new PropertyBuilder().name("hello").build();
        when(widgetRepository.updateProperty("my-widget", "name", requestProperty)).thenThrow(new NotFoundException("page not found"));

        assertThatThrownBy(() ->
                widgetService.updateProperty("my-widget", "name", requestProperty))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    public void should_add_a_property_to_a_widget_and_return_the_list_of_properties() throws Exception {
        Property property = aProperty().build();
        List<Property> expectedProperties = asList(property);
        when(widgetRepository.addProperty("customLabel", property)).thenReturn(expectedProperties);

        widgetService.addProperty("customLabel", property);

        verify(widgetRepository).addProperty("customLabel", property);
    }

    @Test
    public void should_not_allow_to_add_a_property_to_a_pb_widget() throws Exception {
        Property property = aProperty().build();

        assertThatThrownBy(() ->
                widgetService.addProperty("pbLabel", property))
                .isInstanceOf(NotAllowedException.class);
    }

    @Test
    public void should_throw_NotFoundException_when_adding_a_property_to_an_unexisting_widget() throws Exception {
        when(widgetRepository.addProperty(eq("unknownWidget"), any(Property.class))).thenThrow(new NotFoundException("not found"));

        assertThatThrownBy(() ->
                widgetService.addProperty("unknownWidget", aProperty().build()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    public void should_throw_RepositoryException_when_error_appear_while_saving_property() throws Exception {
        when(widgetRepository.addProperty(eq("label"), any(Property.class))).thenThrow(RepositoryException.class);

        assertThatThrownBy(() ->
                widgetService.addProperty("label", aProperty().build()))
                .isInstanceOf(RepositoryException.class);
    }

    @Test
    public void should_update_a_property_of_a_widget_and_return_the_list_of_properties() throws Exception {
        Property property = aProperty().build();
        List<Property> expectedProperties = asList(property);
        when(widgetRepository.updateProperty("customLabel", "toBeUpdated", property)).thenReturn(expectedProperties);

        widgetService.updateProperty("customLabel", "toBeUpdated", property);

        verify(widgetRepository).updateProperty("customLabel", "toBeUpdated", property);
    }

    @Test
    public void should_not_allow_to_update_a_property_of_a_pb_widget() throws Exception {
        assertThatThrownBy(() ->
                widgetService.updateProperty("pbLabel", "toBeUpdated", aProperty().build()))
                .isInstanceOf(NotAllowedException.class);
    }

    @Test
    public void should_throw_NotFoundException_when_widget_or_property_not_found_while_updating_property() throws Exception {
        assertThatThrownBy(() ->
                widgetService.updateProperty("pbLabel", "toBeUpdated", aProperty().build()))
                .isInstanceOf(NotAllowedException.class);
    }

    @Test
    public void should_throw_RepositoryException_when_error_appear_while_updating_property() throws Exception {
        when(widgetRepository.updateProperty(eq("label"), eq("toBeUpdated"), any(Property.class))).thenThrow(RepositoryException.class);

        assertThatThrownBy(() ->
                widgetService.updateProperty("label", "toBeUpdated", aProperty().build()))
                .isInstanceOf(RepositoryException.class);
    }

    @Test
    public void should_delete_a_property_of_a_widget_and_return_the_list_of_properties() throws Exception {
        Property property = aProperty().build();
        List<Property> expectedProperties = asList(property);
        when(widgetRepository.deleteProperty("customLabel", "toBeDeleted")).thenReturn(expectedProperties);

        widgetService.deleteProperty("customLabel", "toBeDeleted");

        verify(widgetRepository).deleteProperty("customLabel", "toBeDeleted");
    }

    @Test
    public void should_not_allow_to_delete_a_property_of_a_pb_widget() throws Exception {
        assertThatThrownBy(() ->
                widgetService.deleteProperty("pbLabel", "toBeDeleted"))
                .isInstanceOf(NotAllowedException.class)
                .hasMessage("Not allowed to modify a non custom widgets");
    }

    @Test
    public void should_throw_NotFoundException_when_widget_or_property_not_found_while_deleting_property() throws Exception {
        when(widgetRepository.deleteProperty("label", "toBeDeleted"))
                .thenThrow(new NotFoundException("Widget [ toBeDeleted ] not found"));

        assertThatThrownBy(() ->
                widgetService.deleteProperty("label", "toBeDeleted"))
                .isInstanceOf(NotFoundException.class);
     }

    @Test
    public void should_respond_500_when_error_appear_while_deleting_property() throws Exception {
        when(widgetRepository.deleteProperty("label", "toBeDeleted")).thenThrow(RepositoryException.class);
        assertThatThrownBy(() ->
                widgetService.deleteProperty("label", "toBeDeleted"))
                .isInstanceOf(RepositoryException.class);
    }

    @Test
    public void should_save_a_local_asset() throws Exception {
        byte[] fileContent = "var hello = 'hello';".getBytes(UTF_8);
        Widget widget = aWidget().withId("my-widget").custom().build();
        when(widgetRepository.get("my-widget")).thenReturn(widget);

        String assetGeneratedId = "assetId";
        when(widgetAssetService.save(eq(widget), any(), eq(fileContent))).thenAnswer(invocationOnMock -> {
            Asset assetToSave = invocationOnMock.getArgument(1);

            assertThat(assetToSave.getId()).isEqualTo(null);

            assetToSave.setId(assetGeneratedId);
            return assetToSave;
        });

        Asset savedAsset = widgetService.saveOrUpdateAsset("my-widget", AssetType.JAVASCRIPT,"myfile.js", fileContent);
        assertThat(savedAsset.getId()).isEqualTo(assetGeneratedId);
        ArgumentCaptor<Asset> assetCaptor = ArgumentCaptor.forClass(Asset.class);
        verify(widgetAssetService).save(eq(widget), assetCaptor.capture(), eq(fileContent));
        Asset assetToSave = assetCaptor.getValue();
        assertThat(assetToSave.getName()).isEqualTo("myfile.js");
        assertThat(assetToSave.getType()).isEqualTo(AssetType.JAVASCRIPT);
        assertThat(assetToSave.getOrder()).isEqualTo(1);
    }

    @Test
    public void should_not_upload_an_asset_for_internal_widget() throws Exception {
        assertThatThrownBy(() ->
            widgetService.saveOrUpdateAsset("pbwidget", AssetType.JAVASCRIPT, "myfile.js", "foo".getBytes()))
                    .isInstanceOf(NotAllowedException.class);
    }

    @Test
    public void should_save_an_external_asset() throws Exception {
        Widget widget = aWidget().withId("my-widget").custom().build();
        Asset expectedAsset = anAsset().withId("assetId").active().withName("myfile.js").withOrder(2).withType(AssetType.JAVASCRIPT).build();
        when(widgetRepository.get("my-widget")).thenReturn(widget);
        when(widgetService.migrate(widget)).thenReturn(widget);

        widgetService.saveAsset("my-widget", expectedAsset);

        verify(widgetAssetService).save(widget, expectedAsset);
    }

    @Test
    public void should_not_save_an_external_asset_for_internal_widget() throws Exception {
        Asset asset = anAsset().build();

        assertThatThrownBy(() ->
            widgetService.saveAsset("pb-widget", asset))
                    .isInstanceOf(NotAllowedException.class);
    }

    @Test
    public void should_not_save_an_external_asset_when_upload_send_an_error() throws Exception {
        Widget widget = aWidget().withId("my-widget").custom().build();
        Asset asset = anAsset().build();
        when(widgetRepository.get("my-widget")).thenReturn(widget);
        when(widgetAssetService.save(widget, asset)).thenThrow(IllegalArgumentException.class);

        assertThatThrownBy(() ->
            widgetService.saveAsset("my-widget", asset))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void should_delete_an_asset() throws Exception {
        Widget widget = aWidget().withId("my-widget").custom().build();
        when(widgetRepository.get("my-widget")).thenReturn(widget);

        widgetService.deleteAsset("my-widget", "UIID");

        verify(widgetAssetService).delete(widget, "UIID");
    }

    @Test
    public void should_increment_an_asset() throws Exception {
        Widget widget = aWidget().withId("my-widget").custom().build();
        when(widgetRepository.get("my-widget")).thenReturn(widget);

        widgetService.changeAssetOrder("my-widget", "UIID", INCREMENT);

        verify(widgetAssetService).changeAssetOrderInComponent(widget, "UIID", INCREMENT);
    }

    @Test
    public void should_decrement_an_asset() throws Exception {
        Widget widget = aWidget().withId("my-widget").custom().build();
        when(widgetRepository.get("my-widget")).thenReturn(widget);

        widgetService.changeAssetOrder("my-widget", "UIID", DECREMENT);

        verify(widgetAssetService).changeAssetOrderInComponent(widget, "UIID", DECREMENT);
    }

    @Test
    public void should_mark_a_widget_as_favorite() throws Exception {
        widgetService.markAsFavorite("my-widget", true);

        verify(widgetRepository).markAsFavorite("my-widget");
    }

    @Test
    public void should_unmark_a_widget_as_favorite() throws Exception {
        widgetService.markAsFavorite("my-widget", false);

        verify(widgetRepository).unmarkAsFavorite("my-widget");
    }

    @Test
    public void should_load_widget_asset_on_disk() throws Exception {
        widgetAssetService.findAssetPath("widget-id", "asset.js", AssetType.JAVASCRIPT.getPrefix());

        verify(widgetAssetService).findAssetPath("widget-id", "asset.js", AssetType.JAVASCRIPT.getPrefix());
    }

    @Test
    public void should_throw_IOException_when_widget_asset_included_in_page_produce_IOException() throws Exception {
        when(widgetAssetService.findAssetPath("widget-id", "asset.js", AssetType.JAVASCRIPT.getPrefix()))
                .thenThrow(new RuntimeException("can't read file"));

        assertThatThrownBy(() ->
            widgetService.findAssetPath("widget-id", "asset.js", AssetType.JAVASCRIPT.getPrefix()))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    public void should_serve_all_light_widgets_in_repository() throws Exception {
        Widget input = aWidget().withId("input").build();
        Widget label = aWidget().withId("label").lastUpdate(parse("2015-02-02T00:00:00.000Z")).build();
        when(widgetRepository.getAll()).thenReturn(asList(input, label));
        String[] ids = {"input", "label"};
        Map<String, List<Page>> map = new HashMap();
        map.put("input", singletonList(aPage().withName("hello").build()));
        when(pageRepository.getArtifactsUsingWidgets(asList(ids))).thenReturn(map);
        when(pageRepository.getComponentName()).thenReturn("page");
        Map<String, List<Fragment>> map2 = new HashMap();
        map2.put("label", singletonList(aFragment().withName("helloFragment").build()));
        when(fragmentRepository.getArtifactsUsingWidgets(asList(ids))).thenReturn(map2);
        when(fragmentRepository.getComponentName()).thenReturn("fragment");
        List<Widget> expectedWidgets = asList(input,label);
        when(widgetRepository.getAll()).thenReturn(expectedWidgets);

        List<Widget> returnedWidgets = widgetService.getAllWithUsedBy();

        assertThat(returnedWidgets).hasSameSizeAs(expectedWidgets);
        assertThat(returnedWidgets).contains(input);
        assertThat(returnedWidgets).contains(label);
        assertThat(input.getUsedBy().size()).isEqualTo(1);
        assertThat(input.getUsedBy().get("page").get(0).getName()).isEqualTo("hello");
        assertThat(label.getUsedBy().size()).isEqualTo(1);
        assertThat(label.getUsedBy().get("fragment").get(0).getName()).isEqualTo("helloFragment");
    }

    @Test
    public void should_not_allow_to_delete_a_custom_widget_used_in_a_fragment() throws Exception {
        when(widgetRepository.get("customLabel")).thenReturn(aWidget().custom().withId("customLabel").build());
        when(pageRepository.getArtifactsUsingWidget("customLabel")).thenReturn(emptyList());
        when(fragmentRepository.getComponentName()).thenReturn("fragment");
        when(fragmentRepository.getArtifactsUsingWidget("customLabel"))
                .thenReturn(asList(aFragment().withName("person").build()));

        assertThatThrownBy(() -> widgetService.delete("customLabel"))
                .isInstanceOf(InUseException.class)
                .hasMessage("The widget cannot be deleted because it is used in 1 fragment, <person>");

    }

    @Test
    public void should_fix_bonds_types_on_save() {
        Property constantTextProperty = aProperty().name("text").bond(CONSTANT).build();
        Property interpolationTextProperty = aProperty().name("text").bond(INTERPOLATION).build();
        Widget persistedWidget = aWidget().withId("labelWidget").modelVersion("2.0").property(constantTextProperty).build();
        lenient().when(widgetRepository.get("labelWidget")).thenReturn(persistedWidget);

        widgetService.updateProperty("labelWidget", "text", interpolationTextProperty);

        verify(bondsTypesFixer).fixBondsTypes("labelWidget", singletonList(interpolationTextProperty));
    }

    @Test
    public void should_migrate_found_widget_when_get_is_called() {
        reset(widgetService);
        Widget widget = aWidget().withId("widget").designerVersion("1.0.0").build();
        Widget widgetMigrated = aWidget().withId("widget").modelVersion("2.0").previousArtifactVersion("1.0.0").build();
        when(widgetRepository.get("widget")).thenReturn(widget);
        MigrationResult<Widget> mr = new MigrationResult(widgetMigrated, Arrays.asList(new MigrationStepReport(MigrationStatus.SUCCESS)));
        when(widgetMigrationApplyer.migrate(widget)).thenReturn(mr);

        widgetService.get("widget");

        verify(widgetMigrationApplyer).migrate(widget);
        verify(widgetRepository).updateLastUpdateAndSave(mr.getArtifact());
    }

    @Test
    public void should_not_update_and_save_widget_if_no_migration_done() {
        reset(widgetService);
        Widget widget = aWidget().withId("widget").modelVersion("2.0").build();
        Widget widgetMigrated = aWidget().withId("widget").modelVersion("2.0").previousArtifactVersion("2.0").build();
        MigrationResult mr = new MigrationResult(widget, Arrays.asList(any(MigrationStepReport.class)));
        lenient().when(widgetMigrationApplyer.migrate(widget)).thenReturn(mr);
        when(widgetRepository.get("widget")).thenReturn(widget);

        widgetService.get("widget");

        verify(widgetMigrationApplyer, never()).migrate(widget);
        verify(widgetRepository, never()).updateLastUpdateAndSave(widgetMigrated);
    }


    @Test
    public void should_migrate_all_custom_widget() throws Exception {
        reset(widgetService);
        Widget widget1 = aWidget().withId("widget1").designerVersion("1.0.0").build();
        Widget widget2 = aWidget().withId("widget2").designerVersion("1.0.0").build();
        Widget widget1Migrated = aWidget().withId("widget1").designerVersion("2.0").build();
        Widget widget2Migrated = aWidget().withId("widget2").designerVersion("2.0").build();
        lenient().when(widgetRepository.get("widget1")).thenReturn(widget1);
        lenient().when(widgetRepository.get("widget2")).thenReturn(widget2);
        when(widgetMigrationApplyer.migrate(widget1)).thenReturn(new MigrationResult(widget1Migrated, Arrays.asList(new MigrationStepReport(MigrationStatus.SUCCESS, "widget1"))));
        when(widgetMigrationApplyer.migrate(widget2)).thenReturn(new MigrationResult(widget2Migrated, Arrays.asList(new MigrationStepReport(MigrationStatus.SUCCESS, "widget2"))));

        Set<String> h = new HashSet<>(Arrays.asList("widget1", "widget1"));
        when(widgetRepository.getByIds(h)).thenReturn(Arrays.asList(widget1, widget2));
        Page page = aPage().with(
                aComponent("widget1"),
                aComponent("widget2"))
                .build();

        when(widgetIdVisitor.visit(page)).thenReturn(h);

        widgetService.migrateAllCustomWidgetUsedInPreviewable(page);

        verify(widgetMigrationApplyer).migrate(widget1);
        verify(widgetMigrationApplyer).migrate(widget2);
    }

    @Test
    public void should_not_update_and_save_widget_if_migration_finish_on_error() {
        reset(widgetService);
        Widget widget = aWidget().withId("widget").modelVersion("1.0").build();
        Widget widgetMigrated = aWidget().withId("widget").modelVersion("2.0").previousArtifactVersion("2.0").build();
        MigrationResult mr = new MigrationResult(widget, Arrays.asList(new MigrationStepReport(MigrationStatus.ERROR)));
        when(widgetMigrationApplyer.migrate(widget)).thenReturn(mr);
        when(widgetRepository.get("widget")).thenReturn(widget);

        widgetService.get("widget");

        verify(widgetMigrationApplyer).migrate(widget);
        verify(widgetRepository, never()).updateLastUpdateAndSave(widgetMigrated);
    }

    @Test
    public void should_get_correct_migration_status_when_dependency_is_to_migrate() throws Exception {
        reset(widgetService);
        Widget widget = aWidget().withId("widget").designerVersion("1.10.0").build();
        Page page = PageBuilder.aPage().withId("myPage").withModelVersion("2.0").build();
        Set<String> ids = new HashSet<>(Arrays.asList("widget"));
        when(widgetRepository.getByIds(ids)).thenReturn(Arrays.asList(widget));
        when(widgetIdVisitor.visit(page)).thenReturn(ids);

        MigrationStatusReport status = widgetService.getMigrationStatusOfCustomWidgetUsed(page);
        Assert.assertEquals(getMigrationStatusReport(true, true), status.toString());
    }

    @Test
    public void should_get_correct_migration_status_when_dependency_is_not_compatible() throws Exception {
        reset(widgetService);
        Page page = PageBuilder.aPage().withId("myPage").withModelVersion("2.0").build();
        Widget widget1 = aWidget().withId("widget1").designerVersion("1.10.0").build();
        Widget widget2 = aWidget().withId("widget2").modelVersion("2.1").isCompatible(false).isMigration(false).build(); //incompatible
        Set<String> ids = new HashSet<>(Arrays.asList("widget1", "widget2"));
        when(widgetRepository.getByIds(ids)).thenReturn(Arrays.asList(widget1, widget2));
        when(widgetIdVisitor.visit(page)).thenReturn(ids);

        MigrationStatusReport status = widgetService.getMigrationStatusOfCustomWidgetUsed(page);
        Assert.assertEquals(getMigrationStatusReport(false, false), status.toString());
    }

    @Test
    public void should_get_correct_migration_status_when_dependency_is_not_to_migrate() throws Exception {
        reset(widgetService);
        Widget widget = aWidget().withId("widget").designerVersion("2.0").isCompatible(true).isMigration(false).build();
        Page page = PageBuilder.aPage().withId("myPage").withModelVersion("2.0").build();
        Set<String> ids = new HashSet<>(Arrays.asList("widget"));
        when(widgetRepository.getByIds(ids)).thenReturn(Arrays.asList(widget));
        when(widgetIdVisitor.visit(page)).thenReturn(ids);

        MigrationStatusReport status = widgetService.getMigrationStatusOfCustomWidgetUsed(page);
        Assert.assertEquals(getMigrationStatusReport(true, false), status.toString());
    }

    private String getMigrationStatusReport(boolean compatible, boolean migration) {
        return new MigrationStatusReport(compatible, migration).toString();
    }

}
