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

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.web.designer.controller.importer.ImportException;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.repository.WidgetRepository;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class WidgetDependencyImporter extends ComponentDependencyImporter<Widget> {

    public static final DirectoryStream.Filter<Path> CUSTOM_WIDGET_FILTER =
            path -> !path.getFileName().toString().startsWith(WidgetRepository.ANGULARJS_STANDARD_PREFIX);

    private final WidgetRepository widgetRepository;

    private final AssetDependencyImporter<Widget> widgetAssetDependencyImporter;

    public WidgetDependencyImporter(WidgetRepository widgetRepository, AssetDependencyImporter<Widget> widgetAssetDependencyImporter) {
        super(widgetRepository);
        this.widgetRepository = widgetRepository;
        this.widgetAssetDependencyImporter = widgetAssetDependencyImporter;
    }

    @Override
    public List<Widget> load(Identifiable parent, Path resources) throws IOException {
        var widgetsPath = resources.resolve("widgets");
        if (Files.exists(widgetsPath)) {
            return widgetRepository.loadAll(widgetsPath, CUSTOM_WIDGET_FILTER);
        }
        return new ArrayList<>();
    }

    @Override
    public void save(List<Widget> elements, Path resources) {

        widgetRepository.saveAll(elements);

        var widgetPath = resources.resolve("widgets");
        for (Widget widget : elements) {
            try {
                final List<Asset> assets = widgetAssetDependencyImporter.load(widget, widgetPath);
                widgetAssetDependencyImporter.save(assets, widgetPath);
            } catch (IOException e) {
                var error = String.format("Technical error when importing widget asset [%s]", widget.getId());
                log.error(error, e);
                throw new ImportException(ImportException.Type.UNEXPECTED_ZIP_STRUCTURE, error);
            }
        }
    }
}
