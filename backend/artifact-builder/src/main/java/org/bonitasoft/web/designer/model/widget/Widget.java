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
package org.bonitasoft.web.designer.model.widget;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.bonitasoft.web.designer.model.Assetable;
import org.bonitasoft.web.designer.model.DesignerArtifact;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.JsonViewLight;
import org.bonitasoft.web.designer.model.JsonViewPersistence;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;

import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.nio.file.Files.readAllBytes;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;


public class Widget extends DesignerArtifact implements Identifiable, Assetable {

    private static final Pattern PATTERN_DATE_VALIDATION = Pattern.compile("[a-zA-Z0-9]*$");
    @Valid
    private final List<Property> properties = new ArrayList<>();
    @NotBlank
    private String id;
    @NotBlank(message = "Widget name should not be blank")
    private String name;
    private Instant lastUpdate; //makes sense only for custom widget
    private String template;
    private String icon;
    private String controller;
    private String description;
    private boolean custom;
    private Integer order;
    private Map<String, List<Identifiable>> usedBy; // list of element that use this widget
    private Set<Asset> assets = new HashSet<>();
    private Set<String> requiredModules = new HashSet<>();
    private Set<String> authRules;
    private String type = "widget";
    private boolean hasHelp = false;

    public static String spinalCase(String widgetId) {
        var firstLetter = Character.toLowerCase(widgetId.charAt(0));
        return firstLetter + widgetId.substring(1).replaceAll("([A-Z])", "-$1").toLowerCase(Locale.ENGLISH);
    }

    /**
     * The validation context can change depending on the nature of a widget. A custom widget name can't contain space but a
     * normal one yes
     */
    @AssertTrue(message = "Widget name should contains only alphanumeric characters with no space")
    private boolean isValidName() {
        if (!isCustom()) {
            return true;
        }
        return PATTERN_DATE_VALIDATION.matcher(name).matches();
    }

    @JsonView({JsonViewLight.class, JsonViewPersistence.class})
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @JsonView({JsonViewLight.class, JsonViewPersistence.class})
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonView({JsonViewLight.class, JsonViewPersistence.class})
    public Instant getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Instant lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    @JsonView({JsonViewPersistence.class})
    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    @JsonView({JsonViewPersistence.class})
    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    @JsonView({JsonViewPersistence.class})
    public String getController() {
        return controller;
    }

    public void setController(String controller) {
        this.controller = controller;
    }

    @JsonView({JsonViewPersistence.class})
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @JsonView({JsonViewLight.class, JsonViewPersistence.class})
    public boolean isCustom() {
        return custom;
    }

    public void setCustom(boolean custom) {
        this.custom = custom;
    }

    @JsonView({JsonViewPersistence.class})
    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    @JsonView({JsonViewPersistence.class})
    public List<Property> getProperties() {
        return Collections.unmodifiableList(properties);
    }

    public void addProperty(Property property) {
        this.properties.add(property);
    }

    public void replaceProperty(Property oldProperty, Property newProperty) {
        Collections.replaceAll(properties, oldProperty, newProperty);
    }

    @JsonView({JsonViewPersistence.class})
    public Set<Asset> getAssets() {
        return assets;
    }

    public void setAssets(Set<Asset> assets) {
        this.assets = assets;
    }

    @Override
    public void addAsset(Asset asset) {
        if (assets == null) {
            assets = new HashSet<>();
        }
        assets.add(asset);
    }

    @Override
    public void addAssets(Set<Asset> assets) {
        if (this.assets == null) {
            this.assets = new HashSet<>();
        }
        this.assets.addAll(assets);
    }

    public void deleteProperty(Property property) {
        this.properties.remove(property);
    }

    public Property getProperty(final String propertyName) {
        return properties.stream().filter(property -> propertyName.equals(property.getName())).findFirst().orElse(null);
    }

    @JsonView({JsonViewLight.class})
    public Map<String, List<Identifiable>> getUsedBy() {
        return usedBy;
    }

    public void addUsedBy(String componantName, List<Identifiable> components) {
        if (components != null && !components.isEmpty()) {
            if (usedBy == null)
                usedBy = new HashMap<>();
            usedBy.put(componantName, components);
        }
    }

    @JsonIgnore
    public boolean isUsed() {
        return getUsedBy() != null && !getUsedBy().isEmpty();
    }

    /**
     * Check if a "Custom" widget is compatible with the currently running UID version.
     *
     * @return true if the widget is compatible
     */
    @JsonIgnore
    public boolean isCompatible() {
        return !isCustom() ||
                (getStatus() == null || getStatus().isCompatible());
    }

    @JsonView({JsonViewPersistence.class})
    public Set<String> getRequiredModules() {
        return requiredModules;
    }

    public void setRequiredModules(Set<String> requiredModules) {
        this.requiredModules = requiredModules;
    }

    @JsonView({JsonViewPersistence.class})
    public Set<String> getAuthRules() {
        return authRules;
    }

    public void setAuthRules(Set<String> authRules) {
        this.authRules = authRules;
    }

    @Override
    public String getType() {
        return type;
    }

    public void setType(String type) {
        if (type != null) {         // default type is widget
            this.type = type;
        }
    }

    @JsonView({JsonViewPersistence.class})
    public boolean hasHelp() {
        return hasHelp;
    }

    public void setHasHelp(boolean hasHelp) {
        this.hasHelp = hasHelp;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Widget) {
            final Widget other = (Widget) obj;
            return new EqualsBuilder()
                    .append(id, other.id)
                    .append(name, other.name)
                    .append(template, other.template)
                    .append(controller, other.controller)
                    .append(custom, other.custom)
                    .append(properties, other.properties)
                    .append(hasHelp, other.hasHelp)
                    .isEquals();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(name)
                .append(template)
                .append(controller)
                .append(custom)
                .append(properties)
                .append(hasHelp)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
                .append("id", id)
                .append("name", name)
                .append("template", template)
                .append("controller", controller)
                .append("custom", custom)
                .append("properties", properties)
                .append("hasHelp", hasHelp)
                .toString();
    }


    /**
     * Prepare object to deserialize     *
     */
    public void prepareWidgetToDeserialize(Path directory) throws IOException {
        loadControllerFromFileContent(directory);
        loadTemplateFromFileContent(directory);
    }


    /**
     * Prepare object to serialize
     */
    public void prepareWidgetToSerialize() {
        if (this.getTemplate() != null && !this.getTemplate().startsWith("@")) {
            var templateFileName = "@" + this.getId() + ".tpl.html";
            this.setTemplate(templateFileName);
        }
        if (this.getController() != null && !this.getController().startsWith("@")) {
            var controllerFileName = "@" + this.getId() + ".ctrl.js";
            this.setController(controllerFileName);
        }
    }

    /**
     * Load content from widget controller file (*.ctrl.js) and set it in controller field
     *
     * @throws IOException
     */
    private void loadControllerFromFileContent(Path directory) throws IOException {
        if (this.getController() != null && this.getController().startsWith("@")) {
            var controllerFileName = this.getController().substring(1);
            var controllerFile = directory.resolve(format("%s", controllerFileName));
            if (!Files.exists(controllerFile)) {
                throw new NotFoundException(format("Controller not found for [%s]", controllerFile.getFileName()));
            }
            this.setController(new String(readAllBytes(controllerFile), StandardCharsets.UTF_8));
        }
    }

    /**
     * Load content from widget template file (*.tpl.html) and set it in template field
     *
     * @throws IOException
     */
    private void loadTemplateFromFileContent(Path directory) throws IOException {
        if (this.getTemplate() != null && this.getTemplate().startsWith("@")) {
            var templateFileName = this.getTemplate().substring(1);
            var templateFile = directory.resolve(format("%s", templateFileName));
            if (!Files.exists(templateFile)) {
                throw new NotFoundException(format("Template not found for [%s]", templateFile.getFileName()));
            }
            this.setTemplate(new String(readAllBytes(templateFile), StandardCharsets.UTF_8));
        }
    }


}
