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
package org.bonitasoft.web.designer.controller.importer.dependencies;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.web.designer.controller.importer.ImportException;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.WidgetLoader;
import org.bonitasoft.web.designer.repository.WidgetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WidgetImporter implements ComponentDependencyImporter<Widget> {

    protected static final Logger logger = LoggerFactory.getLogger(WidgetImporter.class);
    private WidgetLoader widgetLoader;
    private WidgetRepository widgetRepository;
    private AssetImporter<Widget> widgetAssetImporter;

    public WidgetImporter(WidgetLoader widgetLoader, WidgetRepository widgetRepository, AssetImporter<Widget> widgetAssetImporter) {
        this.widgetLoader = widgetLoader;
        this.widgetRepository = widgetRepository;
        this.widgetAssetImporter = widgetAssetImporter;
    }

    @Override
    public List<Widget> load(Identifiable parent, Path resources) throws IOException {
        Path widgetsPath = resources.resolve("widgets");
        if (Files.exists(widgetsPath)) {
            return widgetLoader.getAllCustom(widgetsPath);
        }
        return new ArrayList<>();
    }

    @Override
    public void save(List<Widget> elements, Path resources) {
        Path widgetPath = resources.resolve("widgets");

        widgetRepository.saveAll(elements);
        for (Widget widget : elements) {
            try {
                widgetAssetImporter.save(
                        widgetAssetImporter.load(widget, widgetPath),
                        widgetPath
                );
            } catch (IOException e) {
                String error = String.format("Technical error when importing widget asset [%s]", widget.getId());
                logger.error(error, e);
                throw new ImportException(ImportException.Type.UNEXPECTED_ZIP_STRUCTURE, error);
            }
        }
    }

    @Override
    public String getComponentName() {
        return widgetRepository.getComponentName();
    }

    @Override
    public boolean exists(Widget widget) {
        return widgetRepository.exists(widget.getId());
    }
}
