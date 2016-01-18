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
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.bonitasoft.web.designer.controller.export.steps.ExportStep.RESOURCES;
import static org.springframework.util.FileCopyUtils.copy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.bonitasoft.web.designer.controller.export.steps.ExportStep;
import org.bonitasoft.web.designer.controller.utils.MimeType;
import org.bonitasoft.web.designer.model.DesignerArtifact;
import org.bonitasoft.web.designer.model.JacksonObjectMapper;
import org.bonitasoft.web.designer.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Exporter<T extends DesignerArtifact> {

    private static final Logger logger = LoggerFactory.getLogger(Exporter.class);

    private final JacksonObjectMapper objectMapper;
    private final ExportStep<T>[] exportSteps;

    private Repository<T> repository;

    public Exporter(Repository<T> repository, JacksonObjectMapper objectMapper, ExportStep<T>... exportSteps) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.exportSteps = exportSteps;
    }

    public void handleFileExport(String id, HttpServletResponse resp) throws ServletException {
        if (isBlank(id)) {
            throw new IllegalArgumentException("Id is needed to successfully export a component");
        }

        //We can't write directly in response outputstream. When you start to write a message, you can't remove it after the first flush.
        // If an error occurs you can't prevent a partial file loading. So we need to use a temp stream.
        ByteArrayOutputStream zipStream = null;
        String filename = null;
        //In the first step the zipStream is created
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); Zipper zipper = new Zipper(outputStream)) {
            //The outputStream scope is local in the try-with-resource-block
            zipStream = outputStream;

            T identifiable = repository.get(id);
            filename = getFileName(identifiable);

            // add json model
            zipper.addToZip(objectMapper.toJson(identifiable), format("%s/%s.json", RESOURCES, repository.getComponentName()));
            // forceExecution export steps
            for (ExportStep exporter : exportSteps) {
                exporter.execute(zipper, identifiable);
            }

        } catch (Exception e) {
            logger.error(format("Technical error on zip creation %s with id %s", repository.getComponentName(), id), e);
            throw new ExportException(e);
        }

        //In the second step (we can't do that in the first block, because the zip has to be closed before to be able to read it)
        //the stream is stored in the servlet response
        if (zipStream != null) {
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(zipStream.toByteArray()); ServletOutputStream servletOutputStream = resp.getOutputStream();) {
                resp.setContentType(MimeType.APPLICATION_ZIP.toString());
                resp.setHeader("Content-Disposition", format("inline; filename=%s;", filename));
                copy(inputStream, servletOutputStream);
            } catch (Exception e) {
                logger.error(format("Technical error when exporting %s with id %s", repository.getComponentName(), id), e);
                throw new ExportException(e);
            }
        }
    }

    /**
     * Generates the filename. It has to be placed in the header before the first writting in the stream
     */
    private String getFileName(DesignerArtifact artifact) {
        return format("%s-%s.zip", artifact.getType(), escape(artifact.getName()));
    }

    private String escape(String s) {
        return s == null ? null : s.replace(' ', '-').replaceAll("[^a-zA-Z0-9-]", "");
    }
}
