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
package org.bonitasoft.web.designer.controller.export;

import static java.lang.String.format;
import static org.apache.commons.io.IOUtils.copy;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.bonitasoft.web.designer.controller.export.steps.ExportStep.RESOURCES;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.bonitasoft.web.designer.controller.export.steps.ExportStep;
import org.bonitasoft.web.designer.model.DesignerArtifact;
import org.bonitasoft.web.designer.model.JsonHandler;
import org.bonitasoft.web.designer.model.JsonViewPersistence;
import org.bonitasoft.web.designer.model.ModelException;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.service.ArtifactService;

public abstract class Exporter<T extends DesignerArtifact> {

    protected final ExportStep<T>[] exportSteps;

    protected final ArtifactService<T> artifactService;

    protected JsonHandler jsonHandler;

    @SafeVarargs
    protected Exporter(JsonHandler jsonHandler, ArtifactService<T> artifactService, ExportStep<T>... exportSteps) {
        this.jsonHandler = jsonHandler;
        this.artifactService = artifactService;
        this.exportSteps = exportSteps;
    }

    protected abstract String getComponentType();

    public void handleFileExport(String id, OutputStream stream) throws ModelException, ExportException, IOException {
        if (isBlank(id)) {
            throw new IllegalArgumentException("Id is needed to successfully export a component");
        }

        //We can't write directly in response outputstream. When you start to write a message, you can't remove it after the first flush.
        // If an error occurs you can't prevent a partial file loading. So we need to use a temp stream.
        ByteArrayOutputStream zipStream;
        //In the first step the zipStream is created
        try (var outputStream = new ByteArrayOutputStream(); Zipper zipper = new Zipper(outputStream)) {
            //The outputStream scope is local in the try-with-resource-block
            zipStream = outputStream;

            var identifiable = artifactService.get(id);
            if (identifiable.getStatus() == null) {
                identifiable.setStatus(artifactService.getStatus(identifiable));
            }

            if (!identifiable.getStatus().isCompatible()) {
                throw new ModelException(String.format("%s build failed. A newer UI Designer version is required.", identifiable.getName()));
            }

            if (identifiable instanceof Widget) {
                ((Widget) identifiable).prepareWidgetToSerialize();
            }

            final byte[] json = jsonHandler.toJson(identifiable, JsonViewPersistence.class);
            zipper.addToZip(json, format("%s/%s.json", RESOURCES, getComponentType()));
            // forceExecution export steps
            for (ExportStep<T> exporter : exportSteps) {
                exporter.execute(zipper, identifiable);
            }

        } catch (ModelException e) {
            throw e;
        } catch (Exception e) {
            throw new ExportException(format("Technical error on zip creation %s with id %s", getComponentType(), id), e);
        }

        //Copy work/zip stream content to response stream (we can't do that in the first block, because the zip has to be closed before to be able to read it)
        try (var inputStream = new ByteArrayInputStream(zipStream.toByteArray())) {
            copy(inputStream, stream);
        } catch (Exception e) {
            throw new ExportException(format("Technical error when exporting %s with id %s", getComponentType(), id), e);
        }
    }

}
