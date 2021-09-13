package org.bonitasoft.web.designer;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bonitasoft.web.designer.controller.export.ExportException;
import org.bonitasoft.web.designer.controller.export.FragmentExporter;
import org.bonitasoft.web.designer.controller.export.PageExporter;
import org.bonitasoft.web.designer.controller.export.WidgetExporter;
import org.bonitasoft.web.designer.controller.importer.AbstractArtifactImporter;
import org.bonitasoft.web.designer.controller.importer.FragmentImporter;
import org.bonitasoft.web.designer.controller.importer.ImportException;
import org.bonitasoft.web.designer.controller.importer.ImportStore;
import org.bonitasoft.web.designer.controller.importer.PageImporter;
import org.bonitasoft.web.designer.controller.importer.WidgetImporter;
import org.bonitasoft.web.designer.controller.importer.report.ImportReport;
import org.bonitasoft.web.designer.migration.Version;
import org.bonitasoft.web.designer.model.ModelException;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.rendering.GenerationException;
import org.bonitasoft.web.designer.rendering.HtmlGenerator;
import org.bonitasoft.web.designer.rendering.angular.AngularAppGenerator;
import org.bonitasoft.web.designer.repository.exception.NotFoundException;
import org.bonitasoft.web.designer.service.FragmentService;
import org.bonitasoft.web.designer.service.PageService;
import org.bonitasoft.web.designer.service.WidgetService;
import org.bonitasoft.web.designer.workspace.Workspace;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.bonitasoft.web.designer.controller.importer.ImportException.Type.MODEL_NOT_FOUND;
import static org.bonitasoft.web.designer.controller.importer.ImportPathResolver.resolveImportPath;
import static org.bonitasoft.web.designer.controller.importer.report.ImportReport.Status.IMPORTED;

@RequiredArgsConstructor
public class DefaultArtifactBuilder implements ArtifactBuilder {

    public static final List<String> supportedArtifactTypes = List.of("page", "fragment", "widget");

    @Getter
    private final Workspace workspace;
    private final WidgetService widgetService;
    private final FragmentService fragmentService;
    private final PageService pageService;
    private final PageExporter pageExporter;
    private final FragmentExporter fragmentExporter;
    private final WidgetExporter widgetExporter;
    private final HtmlGenerator htmlGenerator;
    private final AngularAppGenerator<Page> angularAppGenerator;
    private final ImportStore importStore;
    private final PageImporter pageImporter;
    private final FragmentImporter fragmentImporter;
    private final WidgetImporter widgetImporter;

    @Override
    public byte[] buildPage(String id) throws ModelException, ExportException, IOException {
        return build(pageService.get(id));
    }

    @Override
    public byte[] build(Page page) throws ModelException, ExportException, IOException {
        var outputStream = new ByteArrayOutputStream();
        try (outputStream) {
            pageExporter.handleFileExport(page.getId(), outputStream);
        }
        return outputStream.toByteArray();
    }

    @Override
    public byte[] buildFragment(String id) throws ModelException, ExportException, IOException {
        return build(fragmentService.get(id));
    }

    @Override
    public byte[] build(Fragment fragment) throws ModelException, ExportException, IOException {
        var outputStream = new ByteArrayOutputStream();
        try (outputStream) {
            fragmentExporter.handleFileExport(fragment.getId(), outputStream);
        }
        return outputStream.toByteArray();
    }

    @Override
    public byte[] buildWidget(String id) throws ModelException, ExportException, IOException {
        return build(widgetService.get(id));
    }

    @Override
    public byte[] build(Widget widget) throws ModelException, ExportException, IOException {
        var outputStream = new ByteArrayOutputStream();
        try (outputStream) {
            widgetExporter.handleFileExport(widget.getId(), outputStream);
        }
        return outputStream.toByteArray();
    }

    @Override
    public String buildHtml(Page page, String context) throws GenerationException, NotFoundException {
        if (Version.isV3Version(page.getModelVersion())) {
            angularAppGenerator.generateAngularApp(page);
            return "Artifact is generated";
        } else {
            return htmlGenerator.generateHtml(page, context);
        }

    }

    @Override
    public String buildHtml(Fragment fragment, String context) throws GenerationException, NotFoundException {
        return htmlGenerator.generateHtml(fragment, context);
    }

    @Override
    public ImportReport importArtifact(Path path, boolean ignoreConflicts) {

        var zipFiles = resolveImportPath(path);

        var artifactType = resolveArtifactType(zipFiles);
        ImportReport report = null;
        switch (artifactType) {
            case "page":
                report = importPage(path, ignoreConflicts);
                break;
            case "fragment":
                report = importFragment(path, ignoreConflicts);
                break;
            case "widget":
                report = importWidget(path, ignoreConflicts);
                break;
            default:
                // Should never happen since resolveArtifactType() should have already thrown an exception.
                throw new ImportException(MODEL_NOT_FOUND, "Unknown artifact type: " + artifactType);
        }
        return report;
    }

    protected String resolveArtifactType(Path zipFiles) {
        return supportedArtifactTypes.stream()
                .filter(
                        type -> Files.exists(zipFiles.resolve(type + ".json"))
                ).findFirst()
                .orElseThrow(() -> {
                    var importException = new ImportException(MODEL_NOT_FOUND, "Could not load component, artifact model file not found");
                    importException.addInfo("modelfiles", supportedArtifactTypes.stream().map(type -> type + ".json").collect(toList()));
                    return importException;
                });
    }

    @Override
    public ImportReport importPage(Path path, boolean ignoreConflicts) {
        return importFromPath(path, ignoreConflicts, pageImporter);
    }

    @Override
    public ImportReport importFragment(Path path, boolean ignoreConflicts) {
        return importFromPath(path, ignoreConflicts, fragmentImporter);
    }

    @Override
    public ImportReport importWidget(Path path, boolean ignoreConflicts) {
        return importFromPath(path, ignoreConflicts, widgetImporter);
    }

    @Override
    public ImportReport replayImportIgnoringConflicts(String uuid) {
        var anImport = importStore.get(uuid);
        return anImport.getImporter().tryToImportAndGenerateReport(anImport, true);
    }

    @Override
    public void cancelImport(String uuid) {
        importStore.remove(uuid);
    }

    protected ImportReport importFromPath(Path path, boolean ignoreConflicts, AbstractArtifactImporter<?> importer) {
        var anImport = importStore.store(importer, path);
        ImportReport report = null;
        try {
            report = importer.tryToImportAndGenerateReport(anImport, ignoreConflicts);
        } finally {
            if (report == null || IMPORTED.equals(report.getStatus())) {
                importStore.remove(anImport.getUUID());
            }
        }
        return report;
    }
}
