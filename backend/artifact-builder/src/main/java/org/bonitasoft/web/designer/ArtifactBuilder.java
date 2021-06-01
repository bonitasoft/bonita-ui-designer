package org.bonitasoft.web.designer;

import org.bonitasoft.web.designer.controller.export.ExportException;
import org.bonitasoft.web.designer.controller.importer.report.ImportReport;
import org.bonitasoft.web.designer.model.ModelException;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.rendering.GenerationException;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.workspace.Workspace;

import java.io.IOException;
import java.nio.file.Path;

public interface ArtifactBuilder {

    Workspace getWorkspace();

    byte[] buildPage(String id) throws ModelException, ExportException, IOException;
    byte[] build(Page page) throws ModelException, ExportException, IOException;

    byte[] buildFragment(String id) throws ModelException, ExportException, IOException;
    byte[] build(Fragment fragment) throws ModelException, ExportException, IOException;

    byte[] buildWidget(String id) throws ModelException, ExportException, IOException;
    byte[] build(Widget widget) throws ModelException, ExportException, IOException;

    String buildHtml(Page page, String context) throws GenerationException, NotFoundException;

    String buildHtml(Fragment fragment, String context) throws GenerationException, NotFoundException;

    /**
     * Import an artifact
     *
     * @param path            the path to import artifact from
     * @param ignoreConflicts if false, one can choose to {@link #replayImportIgnoringConflicts(String)} in case of conflicting resources
     * @return the report import, it's uuid can be used to rerun and ignoreConflicts import in case of conflicts
     */
    ImportReport importArtifact(Path path, boolean ignoreConflicts);

    ImportReport importPage(Path path, boolean ignoreConflicts);

    ImportReport importFragment(Path path, boolean ignoreConflicts);

    ImportReport importWidget(Path path, boolean ignoreConflicts);

    /**
     * Allow to replay and force an import that overrides existing resources
     *
     * @param uuid the uuid of the previous import with conflicts
     * @return the report import
     */
    ImportReport replayImportIgnoringConflicts(String uuid);

    void cancelImport(String uuid);
}
