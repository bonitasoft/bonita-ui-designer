package org.bonitasoft.web.designer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.bonitasoft.web.designer.migration.JacksonDeserializationProblemHandler;
import org.bonitasoft.web.designer.model.JacksonJsonHandler;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.Container;
import org.bonitasoft.web.designer.model.page.FormContainer;
import org.bonitasoft.web.designer.model.page.FragmentElement;
import org.bonitasoft.web.designer.model.page.ModalContainer;
import org.bonitasoft.web.designer.model.page.TabContainer;
import org.bonitasoft.web.designer.model.page.TabsContainer;

import static com.fasterxml.jackson.databind.DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS;

public class JsonHandlerFactory {

    /**
     * We use our own default json Mapper
     */
    public JsonHandler create() {
        return new JacksonJsonHandler(objectMapper());
    }

    private ObjectMapper objectMapper() {
        var objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        objectMapper.enable(WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.disable(WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS);
        objectMapper.disable(READ_DATE_TIMESTAMPS_AS_NANOSECONDS);

        //By default all properties without explicit view definition are included in serialization.
        //To use JsonView we have to change this parameter
        objectMapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false);

        //We don't have to serialize null values
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        // Required for abstract Element deserialization
        objectMapper.registerSubtypes(
                FragmentElement.class, Component.class, Container.class, FormContainer.class, TabsContainer.class, TabContainer.class, ModalContainer.class
        );

        //add Handler to migrate old json
        objectMapper.addHandler(new JacksonDeserializationProblemHandler());

        //disable filter name check so that filtering is optional
        var simpleFilterProvider = new SimpleFilterProvider();
        simpleFilterProvider.setFailOnUnknownId(false);
        objectMapper.setFilterProvider(simpleFilterProvider);

        return objectMapper;
    }

}
