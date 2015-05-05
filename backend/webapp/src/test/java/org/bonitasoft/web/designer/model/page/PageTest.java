/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.web.designer.model.page;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bonitasoft.web.designer.ApplicationConfig;
import org.bonitasoft.web.designer.builder.PageBuilder;
import org.bonitasoft.web.designer.config.ContextConfigTest;
import org.bonitasoft.web.designer.model.JsonViewLight;
import org.bonitasoft.web.designer.model.JsonViewPersistence;
import org.bonitasoft.web.designer.repository.BeanValidator;
import org.bonitasoft.web.designer.repository.exception.ConstraintValidationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ContextConfigTest.class , ApplicationConfig.class })
@WebAppConfiguration("src/test/resources")
public class PageTest {

    @Inject
    private ObjectMapper objectMapper;

    @Inject
    private BeanValidator beanValidator;

    @Test
    public void jsonview_light_should_only_manage_id_and_name() throws Exception {
        String json = objectMapper.writerWithView(JsonViewLight.class).writeValueAsString(createAFilledPage());
        assertThat(json).isEqualTo("{\"id\":\"UUID\",\"name\":\"myPage\"}");
    }

    @Test
    public void jsonview_persistence_should_manage_all_properties() throws Exception {
        Page myPage = createAFilledPage();

        //We serialize and deserialize our object
        Page pageAfterJsonProcessing = objectMapper.readValue(
                objectMapper.writerWithView(JsonViewPersistence.class).writeValueAsString(myPage),
                Page.class);

        assertThat(pageAfterJsonProcessing.getName()).isEqualTo(myPage.getName());
        assertThat(pageAfterJsonProcessing.getId()).isEqualTo(myPage.getId());
        assertThat(pageAfterJsonProcessing.getData()).isNotEmpty();
        assertThat(pageAfterJsonProcessing.getRows()).isNotEmpty();

        //A rows contains a list of elements. We verify the first
        Element element = pageAfterJsonProcessing.getRows().get(0).get(0);
        assertThat(element.getPropertyValues()).isNotNull();
        assertThat(element.getDimension().get("xs")).isEqualTo(12);

    }

    @Test
    public void jsonview_persitence_should_manage_all_fields() throws Exception {
        Page pageInitial = createAFilledPage();
        //We serialize and deserialize our object
        Page pageAfterJsonProcessing = objectMapper.readValue(
                objectMapper.writeValueAsString(pageInitial),
                Page.class);

        assertThat(pageAfterJsonProcessing.getName()).isNotNull();
        assertThat(pageAfterJsonProcessing.getId()).isNotNull();
        assertThat(pageAfterJsonProcessing.getData()).isNotEmpty();
        assertThat(pageAfterJsonProcessing.getRows()).isNotNull();
    }

    @Test(expected = ConstraintValidationException.class)
    public void should_not_have_a_name_containing_space() throws Exception {
        Page page = aPage().withName("the name").build();

        beanValidator.validate(page);
    }

    @Test(expected = ConstraintValidationException.class)
    public void should_not_have_a_name_containing_special_characters() throws Exception {
        Page page = aPage().withName("the-name").build();

        beanValidator.validate(page);
    }

    /**
     * Create a filled page with a value for all fields
     */
    private Page createAFilledPage() throws Exception {
        Page page = PageBuilder.aFilledPage("UUID");
        page.setName("myPage");
        return page;
    }
}
