package org.bonitasoft.web.designer.service;

import org.bonitasoft.web.designer.controller.ArtifactInfo;
import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.page.Page;

import java.util.List;
import java.util.Set;

public interface PageService extends AssetableArtifactService<Page> {

    List<Page> getAll();

    ArtifactInfo getInfo(String pageId);

    Page create(Page page);

    Page createFrom(String sourcePageId, Page page);

    Page save(String pageId, Page page);

    Page rename(String pageId, String name);

    void delete(String pageId);

    List<String> getResources(Page page);

    Set<Asset> listAsset(Page page);
}
