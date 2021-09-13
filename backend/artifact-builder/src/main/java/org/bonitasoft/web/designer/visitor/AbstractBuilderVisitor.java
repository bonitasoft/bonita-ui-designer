package org.bonitasoft.web.designer.visitor;

import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.page.Previewable;

public interface AbstractBuilderVisitor<T> {
    <P extends Previewable & Identifiable> T build(final P previewable, String resourceContext);
}
