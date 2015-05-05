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
package org.bonitasoft.web.designer.rendering;

import javax.inject.Inject;
import javax.inject.Named;

import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.page.Previewable;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;
import org.bonitasoft.web.designer.visitor.HtmlBuilderVisitor;
import org.jsoup.Jsoup;

@Named
public class HtmlGenerator {

    private HtmlBuilderVisitor htmlBuilderVisitor;

    @Inject
    public HtmlGenerator(HtmlBuilderVisitor htmlBuilderVisitor) {
        this.htmlBuilderVisitor = htmlBuilderVisitor;
    }

    public  <P extends Previewable & Identifiable> String generateHtml(P previewable) throws GenerationException, NotFoundException {
       return generateHtml("", previewable);
    }

    public <P extends Previewable & Identifiable> String generateHtml(String resourceContext, P previewable) throws GenerationException, NotFoundException {
        try {
            return format(htmlBuilderVisitor.build(previewable, resourceContext));
        } catch (RepositoryException e) {
            throw new GenerationException("Error while generating page", e);
        }
    }

    private String format(String html) {
        return Jsoup.parse(html).toString();
    }
}
