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
package org.bonitasoft.web.designer.controller.export;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.bonitasoft.web.designer.controller.export.steps.ExportStep.RESOURCES;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.bonitasoft.web.designer.controller.export.steps.ExportStep;
import org.bonitasoft.web.designer.controller.utils.MimeType;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.JacksonObjectMapper;
import org.bonitasoft.web.designer.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Exporter<T extends Identifiable> {

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

        try (ServletOutputStream outputStream = resp.getOutputStream(); Zipper zipper = new Zipper(outputStream)) {
            T identifiable = repository.get(id);

            resp.setContentType(MimeType.APPLICATION_ZIP.toString());
            resp.setHeader("Content-Disposition", String.format("inline; filename=%s;", getFileName(identifiable)));

            // add json model
            zipper.addToZip(objectMapper.toJson(identifiable), String.format("%s/%s.json", RESOURCES, repository.getComponentName()));
            // execute export steps
            for (ExportStep exporter : exportSteps) {
                exporter.execute(zipper, identifiable);
            }
        }
        catch (Exception e) {
            logger.error(String.format("Technical error when exporting %s with id %s", repository.getComponentName(), id), e);
            throw new ServletException(e);
        }
    }

    /**
     * Generates the filename. It has to be placed in the header before the first writting in the stream
     */
    private String getFileName(Identifiable identifiable){
        return String.format("%s-%s.zip", repository.getComponentName(), escape(identifiable.getName()));
    }

    private String escape(String s) {
        return s==null ? null : s.replace(' ', '-').replaceAll("[^a-zA-Z0-9-]", "");
    }
}
