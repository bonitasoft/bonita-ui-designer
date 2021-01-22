package org.bonitasoft.web.designer;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.web.designer.config.AppProperties.BonitaDataProperties;
import org.bonitasoft.web.designer.config.AppProperties.BonitaPortalProperties;
import org.bonitasoft.web.designer.config.AppProperties.DesignerProperties;
import org.bonitasoft.web.designer.config.AppProperties.UidProperties;
import org.bonitasoft.web.designer.workspace.WorkspacePathResolver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties({ DesignerProperties.class, BonitaDataProperties.class, BonitaPortalProperties.class, UidProperties.class })
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class Main {

    @Inject
    private WorkspacePathResolver workspacePathResolver;

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @PostConstruct
    public void initialize() {
        log.info("PagesRepositoryPath: {}", workspacePathResolver.getPagesRepositoryPath());
        log.info("TmpPagesRepositoryPath: {}", workspacePathResolver.getTmpPagesRepositoryPath());

        log.info("FragmentsRepositoryPath: {}", workspacePathResolver.getFragmentsRepositoryPath());
        log.info("TmpFragmentsRepositoryPath: {}", workspacePathResolver.getTmpFragmentsRepositoryPath());

        log.info("WidgetsRepositoryPath: {}", workspacePathResolver.getWidgetsRepositoryPath());
        log.info("WidgetsWcRepositoryPath: {}", workspacePathResolver.getWidgetsWcRepositoryPath());

        log.info("WorkspacePath: {}", workspacePathResolver.getWorkspacePath());
        log.info("TemporaryWorkspacePath: {}", workspacePathResolver.getTemporaryWorkspacePath());

        log.info("TmpI18nRepositoryPath: {}", workspacePathResolver.getTmpI18nRepositoryPath());
    }
}
