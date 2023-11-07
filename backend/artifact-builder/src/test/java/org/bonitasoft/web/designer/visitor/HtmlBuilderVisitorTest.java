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
package org.bonitasoft.web.designer.visitor;

import org.apache.commons.codec.digest.DigestUtils;
import org.bonitasoft.web.designer.builder.ModalContainerBuilder;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.asset.AssetScope;
import org.bonitasoft.web.designer.model.asset.AssetType;
import org.bonitasoft.web.designer.model.page.Container;
import org.bonitasoft.web.designer.model.page.FormContainer;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.page.TabContainer;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.rendering.DirectivesCollector;
import org.bonitasoft.web.designer.rendering.GenerationException;
import org.bonitasoft.web.designer.rendering.HtmlGenerator;
import org.bonitasoft.web.designer.repository.AssetRepository;
import org.bonitasoft.web.designer.repository.FragmentRepository;
import org.bonitasoft.web.designer.repository.WidgetRepository;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;
import org.bonitasoft.web.designer.utils.assertions.CustomAssertions;
import org.bonitasoft.web.designer.utils.rule.TestResource;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.AssetBuilder.anAsset;
import static org.bonitasoft.web.designer.builder.ComponentBuilder.*;
import static org.bonitasoft.web.designer.builder.ContainerBuilder.aContainer;
import static org.bonitasoft.web.designer.builder.FormContainerBuilder.aFormContainer;
import static org.bonitasoft.web.designer.builder.FragmentBuilder.aFragment;
import static org.bonitasoft.web.designer.builder.FragmentElementBuilder.aFragmentElement;
import static org.bonitasoft.web.designer.builder.ModalContainerBuilder.aModalContainer;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.bonitasoft.web.designer.builder.ResponsiveDimension.*;
import static org.bonitasoft.web.designer.builder.RowBuilder.aRow;
import static org.bonitasoft.web.designer.builder.TabContainerBuilder.aTabContainer;
import static org.bonitasoft.web.designer.builder.TabsContainerBuilder.aTabsContainer;
import static org.bonitasoft.web.designer.builder.VariableBuilder.aConstantVariable;
import static org.bonitasoft.web.designer.utils.assertions.CustomAssertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@RunWith(MockitoJUnitRunner.class)
public class HtmlBuilderVisitorTest {

    private static final byte[] assetsContent = new byte[0];

    @Rule
    public TestResource testResource = new TestResource(HtmlGenerator.class);

    @Mock
    private PageFactory pageFactory;

    @Mock
    private RequiredModulesVisitor requiredModulesVisitor;

    @Mock
    private AssetVisitor assetVisitor;

    @Mock
    private AssetRepository<Page> pageAssetRepository;

    @Mock
    private AssetRepository<Widget> widgetAssetRepository;

    @Mock
    private FragmentRepository fragmentRepository;

    @Mock
    private WidgetRepository widgetRepository;

    @Mock
    private PropertyValuesVisitor propertyValuesVisitor;

    @Mock
    private VariableModelVisitor variableModelVisitor;

    @Mock
    private WidgetIdVisitor widgetIdVisitor;

    private HtmlBuilderVisitor visitor;

    private String assetSHA1;

    @Mock
    private DirectivesCollector directivesCollector;

    private String modelVersion = "2.2";

    @Before
    public void setUp() throws Exception {
        visitor = new HtmlBuilderVisitor(
                assetVisitor,
                List.of(pageFactory),
                requiredModulesVisitor,
                directivesCollector,
                pageAssetRepository,
                widgetAssetRepository,
                fragmentRepository,
                modelVersion
        );
        when(requiredModulesVisitor.visit(any(Page.class))).thenReturn(Collections.emptySet());
        when(pageAssetRepository.readAllBytes(anyString(), any(Asset.class))).thenReturn(assetsContent);
        when(widgetAssetRepository.readAllBytes(any(Asset.class))).thenReturn(assetsContent);
        assetSHA1 = DigestUtils.sha1Hex(assetsContent);
    }

    @Test
    public void should_build_a_component_html_when_visiting_a_component() throws Exception {
        assertThatHtmlBody(visitor.visit(aComponent("pbWidget")
                .withReference("component-reference")
                .withPropertyValue("property", "value")
                .build())).isEqualToBody(testResource.load("component.html"));
    }

    @Test
    public void should_add_dimension_to_component() throws Exception {

        Element element = CustomAssertions.toBody(visitor.visit(aComponent("pbWidget")
                .withPropertyValue("property", "value")
                .withDimensions(sm(3))
                .build()));

        assertThatHtmlBody(element.childNode(1).outerHtml()).hasClass("col-xs-12", "col-sm-3");
    }

    @Test
    public void should_build_a_container() throws GenerationException {

        assertThatHtmlBody(visitor.visit(aContainer()
                .withReference("container-reference")
                .withPropertyValue("property", "value")
                .build())).isEqualToBody(testResource.load("simplecontainer.html"));
    }

    @Test
    public void should_add_rows_to_the_container() throws Exception {

        assertThatHtmlBody(visitor.visit(aContainer().with(aRow()).withReference("container-reference").build()))
                .isEqualToBody(testResource.load("containerWithRow.html"));
    }

    @Test
    public void should_build_a_repeatable_container() throws GenerationException {

        final String html = visitor.visit(
                aContainer()
                        .withReference("container-reference")
                        .withPropertyValue("repeatedCollection", "json", "[\"foo\",\"bar\"]")
                        .build()
        );
        assertThatHtmlBody(html)
                .isEqualToBody(testResource.load("repeatedContainer.html"));
    }

    @Test
    public void should_not_build_a_repeatable_container_if_repeated_collection_is_an_empty_string() throws GenerationException {

        assertThatHtmlBody(visitor.visit(aContainer().withReference("container-reference")
                .withPropertyValue("repeatedCollection", "json", "")
                .build())).isEqualToBody(testResource.load("notRepeatedContainer.html"));
    }

    @Test
    public void should_add_dimension_to_the_container() throws GenerationException {
        Container container = aContainer().withDimensions(xs(5), sm(7), md(9), lg(10)).build();

        String html = visitor.visit(container);

        assertThatHtmlBody(html).isEqualToBody(testResource.load("containerWithDimension.html"));
    }

    @Test
    public void should_add_elements_to_the_container_rows() throws Exception {

        // we should have two div.col-xs-12 with two div.row containing added components
        Elements rows = toBody(visitor.visit(aContainer().with(
                aRow().with(
                        aComponent().withWidgetId("pbLabel").build()),
                aRow().with(
                        aComponent().withWidgetId("customLabel").build()))
                .build())).select(".row");

        assertThat(rows.size()).isEqualTo(2);
        assertThat(rows.first().select("pb-label").outerHtml()).isEqualTo("<pb-label></pb-label>");
        assertThat(rows.last().select("custom-label").outerHtml()).isEqualTo("<custom-label></custom-label>");
    }

    @Test
    public void should_build_a_tabsContainer_html_when_visiting_a_tabsContainer() throws Exception {
        TabContainer tab = aTabContainer().withId("1").with(aContainer().withReference("first-container")).withReference("tab-container-1").build();
        TabContainer tab1 = aTabContainer().withId("2").with(aContainer().withReference("last-container")).withReference("tab-container-2").build();

        assertThatHtmlBody(visitor.visit(aTabsContainer().with(tab, tab1)
                .withReference("tabs-container-reference")
                .build())).isEqualToBody(testResource.load("tabsContainerWithTwoTabs.html"));
    }

    @Test
    public void should_build_a_tab_container_bootstrap_like() throws Exception {

        assertThatHtmlBody(visitor.visit(aTabsContainer()
                .withReference("tabs-container-reference")
                .withDimension(4)
                .withPropertyValue("property", "value")
                .build())).isEqualToBody(testResource.load("simpleTabContainer.html"));
    }

    @Test
    public void should_add_elements_to_the_tab_container_tabs() throws Exception {
        TabContainer tab = aTabContainer()
                .withId("1")
                .with(aContainer()
                        .with(aRow().with(aParagraph().withReference("paragraph-reference")))
                        .withReference("container-reference"))
                .withReference("tab-reference").build();
        assertThatHtmlBody(visitor.visit(aTabsContainer()
                .with(tab)
                .withReference("tabs-container-reference")
                .build())).isEqualToBody(testResource.load("tabsContainerWithContent.html"));
    }

    @Test
    public void should_build_a_container_fluid_for_a_previewable() throws Exception {
        Page page = aPage().build();
        when(pageFactory.generate(page)).thenReturn("var foo = \"bar\";");

        assertThatHtmlBody(visitor.build(page, "mycontext/")).hasElement("div.container-fluid");
    }

    @Test
    public void should_generate_an_html_with_the_model_version_in_the_js_resources_URLs() throws Exception {
        Page page = aPage().withId("page-id").build();
        when(pageFactory.generate(page)).thenReturn("var foo = \"bar\";");

        // when we generate the html
        String generatedHtml = visitor.build(page, "mycontext/");

        // then we should have the directive scripts included
        assertThat(generatedHtml)
                .contains("<script src=\"mycontext/js/vendor.min.js?modelVersion=" + modelVersion + "\"></script>")
                .contains("<script src=\"mycontext/js/runtime.min.js?modelVersion=" + modelVersion + "\"></script>")
                .contains("pb-model='page-id'"); // and an empty object as constant
    }

    @Test
    public void should_generate_an_html_with_the_list_of_widgets() throws Exception {
        Page page = aPage().withId("page-id").build();
        when(pageFactory.generate(page)).thenReturn("var foo = \"bar\";");
        when(directivesCollector.buildUniqueDirectivesFiles(page, page.getId())).thenReturn(Arrays.asList("assets/widgets.js"));

        // when we generate the html
        String generatedHtml = visitor.build(page, "mycontext/");

        // then we should have the directive scripts included
        assertThat(generatedHtml).contains("<script src=\"assets/widgets.js\"></script>");

        // and an empty object as constant
        assertThat(generatedHtml).contains("pb-model='page-id'");
    }

    @Test
    public void should_generate_html_for_a_page() throws Exception {
        Asset assetLocal = anAsset().withOrder(1)
                .withName("bonita.vendors.js").withExternal(false)
                .build();
        Asset assetJquery = anAsset().withOrder(2)
                .withName("//code.jquery.com/jquery-2.1.4.min.js").withExternal(true)
                .build();
        Asset assetRelative = anAsset().withOrder(3)
                .withName("bonita.min.js").withExternal(true)
                .build();
        Page page = aPage().withId("page-id")
                .withAsset(assetRelative, assetJquery)
                .with(aContainer().with(
                        aRow().with(
                                anInput().withReference("input-reference"),
                                aParagraph().withReference("paragraph-reference")))
                        .withReference("container-reference"))
                .build();
        when(pageFactory.generate(page)).thenReturn("var baz = \"qux\";");
        when(directivesCollector.buildUniqueDirectivesFiles(page, page.getId())).thenReturn(Arrays.asList
                ("assets/widgets-f8b2ef17808cccb95dbf0973e7745cd53c29c684.js"));
        when(assetVisitor.visit(page)).thenReturn(Set.of(assetRelative, assetJquery, assetLocal));

        String html = visitor.build(page, "mycontext/");

        assertThatHtmlBody(html).isEqualToBody(testResource.load("page.html"));
        assertThatHtmlHead(html).isEqualToHead(testResource.load("page.html"));
    }

    @Test
    public void should_generate_html_for_a_page_with_a_custom_display_name_put_in_title_tag() throws Exception {
        Page page = aPage().withId("page-id")
                .withDisplayName("This is a beautiful title for this page")
                .build();
        when(pageFactory.generate(page)).thenReturn("var baz = \"qux\";");

        String html = visitor.build(page, "mycontext/");

        assertThatHtmlBody(html).isEqualToBody(testResource.load("pageCustomDisplayName.html"));
        assertThatHtmlHead(html).isEqualToHead(testResource.load("pageCustomDisplayName.html"));
    }

    @Test
    public void should_generate_html_for_a_formcontainer() throws GenerationException {
        assertThatHtmlBody(
                visitor.visit(aFormContainer()
                        .with(aContainer().withReference("container-reference").build())
                        .withReference("formcontainer-reference")
                        .build()))
                .isEqualToBody(testResource.load("formContainerSimple.html"));
    }

    @Test
    public void should_add_dimension_to_the_formcontainer() throws GenerationException {
        FormContainer formContainer = aFormContainer()
                .with(aContainer().withReference("container-reference").build())
                .withDimensions(xs(5), sm(7), md(9), lg(10)).build();

        String html = visitor.visit(formContainer);

        assertThatHtmlBody(html).isEqualToBody(testResource.load("formContainerWithDimension.html"));
    }

    @Test
    public void should_add_container_to_the_formcontainer() throws Exception {
        FormContainer formContainer = aFormContainer()
                .with(aContainer().with(aRow().with(
                        aComponent().withWidgetId("pbLabel").withReference("component-reference").build()))
                        .withReference("container-reference").build())
                .withReference("formcontainer-reference")
                .build();

        assertThatHtmlBody(visitor.visit(formContainer)).isEqualToBody(testResource.load("formContainerWithContainer.html"));
    }

    /**
     * Test for Modal Container
     */
    @Test
    public void should_build_a_modal_container() throws GenerationException {
        assertThatHtmlBody(visitor.visit(aModalContainer().with(aContainer().withReference("container-reference").build())
                .withReference("modal-container-reference")
                .withPropertyValue("property", "value")
                .withPropertyValue("modalId", "modal1")
                .build())).isEqualToBody(testResource.load("modalContainer.html"));
    }

    @Test
    public void should_add_row_to_the_modal_container() throws Exception {
        ModalContainerBuilder modal = aModalContainer();
        modal.withPropertyValue("modalId", "modal1");
        modal.with(aContainer().with(aRow().build()).withReference("first-container").build());
        assertThatHtmlBody(visitor.visit(modal.build()))
                .isEqualToBody(testResource.load("modalContainerWithRow.html"));
    }

    @Test
    public void should_add_extra_modules_when_widgets_needs_them() throws Exception {
        Page page = aPage().build();
        when(requiredModulesVisitor.visit(page)).thenReturn(Set.of("needed.module"));

        String html = visitor.build(page, "");

        Element head = Jsoup.parse(html).head();
        assertThat(head.html()).contains("angular.module('bonitasoft.ui').requires.push('needed.module');");
    }

    @Test
    public void should_not_add_extra_modules_when_no_widgets_needs_them() throws Exception {
        Page page = aPage().build();
        when(requiredModulesVisitor.visit(page)).thenReturn(Collections.<String>emptySet());

        String html = visitor.build(page, "");

        Element head = Jsoup.parse(html).head();
        assertThat(head.html()).doesNotContain("angular.module('bonitasoft.ui').requires.push");
    }

    @Test
    public void should_add_asset_import_in_header() throws Exception {
        Page page = aPage().build();

        when(assetVisitor.visit(page)).thenReturn(
                Set.of(
                        //A css file in the page
                        new Asset().setName("myfile.css").setType(AssetType.CSS),
                        new Asset().setName("http://moncdn/myfile.css").setExternal(true).setType(AssetType.CSS),
                        //An external css file in the page
                        //A js file in a widget
                        new Asset().setName("myfile.js").setType(AssetType.JAVASCRIPT).setScope(AssetScope.WIDGET).setComponentId("widget-id")));

        String html = visitor.build(page, "mycontext/");

        String head = Jsoup.parse(html).head().html();
        assertThat(head).contains("<link rel=\"stylesheet\" href=\"assets/css/myfile.css?hash=" + assetSHA1 + "\">");
        assertThat(head).contains("<link rel=\"stylesheet\" href=\"http://moncdn/myfile.css\">");
        assertThat(head).contains("<script src=\"widgets/widget-id/assets/js/myfile.js?hash=" + assetSHA1 + "\"></script>");
    }

    @Test
    public void should_build_rows() throws Exception {

        String html = visitor.build(asList(
                aRow().with(aParagraph().withReference("1")).build(),
                aRow().with(anInput().withReference("2"), aParagraph().withReference("3")).build()));

        assertThatHtmlBody(html).isEqualToBody(testResource.load("rowsWithComponents.html"));
    }

    @Test
    public void should_add_active_and_ordered_asset_import_in_header() throws Exception {
        Page page = aPage().build();

        when(assetVisitor.visit(page)).thenReturn(
                Set.of(
                        //Widgets assets
                        new Asset().setName("myfile3.js").setOrder(3).setType(AssetType.JAVASCRIPT).setScope(AssetScope.WIDGET).setComponentId("widget-id"),
                        new Asset().setName("myfile2.js").setOrder(2).setType(AssetType.JAVASCRIPT).setScope(AssetScope.WIDGET).setComponentId("widget-id"),
                        new Asset().setName("myfile99.js").setOrder(99).setActive(false).setType(AssetType.JAVASCRIPT).setScope(AssetScope.WIDGET)
                                .setComponentId("widget-id"),
                        //Another widget but with a name starting with z
                        new Asset().setName("myfile4.js").setOrder(1).setType(AssetType.JAVASCRIPT).setScope(AssetScope.WIDGET).setComponentId("zidget-id"),
                        //Page asset
                        new Asset().setName("myfile1.js").setOrder(0).setType(AssetType.JAVASCRIPT)));

        String html = visitor.build(page, "mycontext/");

        String head = Jsoup.parse(html).head().html();

        //The header not contain inactive asset
        assertThat(head).doesNotContain("myfile99.js");
        //Page asset should be the last one, after widget assets identified by [widget-id] and widget assets identified by [zidget-id]
        assertThat(head).contains("<script src=\"widgets/widget-id/assets/js/myfile2.js?hash=" + assetSHA1 + "\"></script> \n" +
                "<script src=\"widgets/widget-id/assets/js/myfile3.js?hash=" + assetSHA1 + "\"></script> \n" +
                "<script src=\"widgets/zidget-id/assets/js/myfile4.js?hash=" + assetSHA1 + "\"></script> \n" +
                "<script src=\"assets/js/myfile1.js?hash=" + assetSHA1 + "\"></script>");
    }

    @Test
    public void should_import_asset_only_once_for_each_widget() throws Exception {
        Page page = aPage().build();

        var assets = new HashSet<Asset>();
        assets.add(new Asset().setName("myfile5.js").setOrder(0).setType(AssetType.JAVASCRIPT));
        assets.add(new Asset().setName("myfile2.js").setOrder(1).setType(AssetType.JAVASCRIPT).setScope(AssetScope.WIDGET).setComponentId("widget-id"));
        assets.add(new Asset().setName("myfile3.js").setOrder(2).setType(AssetType.JAVASCRIPT).setScope(AssetScope.WIDGET).setComponentId("widget-id"));
        assets.add(new Asset().setName("myfile1.js").setOrder(3).setType(AssetType.JAVASCRIPT).setScope(AssetScope.WIDGET).setComponentId("widget-id"));
        assets.add(new Asset().setName("myfile3.js").setOrder(4).setType(AssetType.JAVASCRIPT).setScope(AssetScope.WIDGET).setComponentId("widget-id"));
        assets.add(new Asset().setName("myfile2.js").setOrder(5).setType(AssetType.JAVASCRIPT).setScope(AssetScope.WIDGET).setComponentId("widget-id"));

        when(assetVisitor.visit(page)).thenReturn(assets);

        String html = visitor.build(page, "mycontext/");

        String head = Jsoup.parse(html).head().html();

        // The page should contain exactly these imports
        assertThat(head).contains(
                "<script src=\"widgets/widget-id/assets/js/myfile2.js?hash=" + assetSHA1 + "\"></script> \n" +
                        "<script src=\"widgets/widget-id/assets/js/myfile3.js?hash=" + assetSHA1 + "\"></script> \n" +
                        "<script src=\"widgets/widget-id/assets/js/myfile1.js?hash=" + assetSHA1 + "\"></script> \n" +
                        "<script src=\"assets/js/myfile5.js?hash=" + assetSHA1 + "\"></script>");
    }

    @Test
    public void should_import_asset_only_once_globally() throws Exception {
        Page page = aPage().build();

        var assets = new HashSet<Asset>();
        assets.add(new Asset().setName("myfile1.js").setOrder(0).setType(AssetType.JAVASCRIPT).setScope(AssetScope.WIDGET).setComponentId("widget-id"));
        assets.add(new Asset().setName("myfile1.js").setOrder(1).setType(AssetType.JAVASCRIPT).setScope(AssetScope.WIDGET).setComponentId("zidget-id"));
        assets.add(new Asset().setName("myfile1.js").setOrder(2).setType(AssetType.JAVASCRIPT).setScope(AssetScope.WIDGET).setComponentId("gidget-id"));
        assets.add(new Asset().setName("myfile1.js").setOrder(3).setType(AssetType.JAVASCRIPT).setScope(AssetScope.WIDGET).setComponentId("vidget-id"));
        assets.add(new Asset().setName("myfile1.js").setOrder(4).setType(AssetType.JAVASCRIPT).setScope(AssetScope.WIDGET).setComponentId("nidget-id"));

        when(assetVisitor.visit(page)).thenReturn(assets);

        String html = visitor.build(page, "mycontext/");

        String head = Jsoup.parse(html).head().html();

        // The page should contain exactly these imports
        assertThat(head).contains(
                "<script src=\"widgets/widget-id/assets/js/myfile1.js?hash=" + assetSHA1 + "\"></script> \n");
    }

    @Test
    public void should_get_html_from_main_container_of_associated_fragment() throws Exception {
        when(fragmentRepository.get("fragment-id")).thenReturn(aFragment()
                .withId("fragment-id")
                .withName("person")
                .withVariable("aKey", aConstantVariable().value("aValue").exposed(true))
                .build());

        assertThatHtmlBody(visitor.visit(aFragmentElement()
                .withFragmentId("fragment-id")
                .withBinding("fragmentVariable1", "pageVariable1")
                .withReference("fragment-reference")
                .build())).isEqualToBody(testResource.load("fragment.html"));
    }

    @Test(expected = GenerationException.class)
    public void should_throw_generation_exception_when_associated_fragment_is_not_found() throws Exception {
        when(fragmentRepository.get("unknown-fragment")).thenThrow(new NotFoundException("not found"));

        visitor.visit(aFragmentElement()
                .withFragmentId("unknown-fragment")
                .build());
    }

    @Test(expected = GenerationException.class)
    public void should_throw_generation_exception_when_error_occurs_while_getting_associated_fragment_from_repository() throws Exception {
        when(fragmentRepository.get("bad-fragment")).thenThrow(new RepositoryException("error", new Exception()));

        visitor.visit(aFragmentElement()
                .withFragmentId("bad-fragment")
                .build());
    }
}
