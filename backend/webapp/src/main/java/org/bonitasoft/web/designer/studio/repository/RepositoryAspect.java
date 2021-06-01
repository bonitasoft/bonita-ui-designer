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
package org.bonitasoft.web.designer.studio.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.bonitasoft.web.designer.config.WorkspaceProperties;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.repository.AbstractRepository;
import org.bonitasoft.web.designer.repository.JsonFileBasedPersister;
import org.bonitasoft.web.designer.repository.Repository;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;
import org.bonitasoft.web.designer.studio.workspace.ResourceNotFoundException;
import org.bonitasoft.web.designer.studio.workspace.WorkspaceResourceHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Path;

import static org.springframework.util.StringUtils.hasText;

/**
 * Use environment variable -Dspring_profiles_active=studio to activate this aspect
 *
 * @author Romain Bioteau
 */
@Slf4j
@ConditionalOnProperty(value = "designer.workspace.apiUrl")
@Aspect
@Component
@RequiredArgsConstructor
public class RepositoryAspect {

    private final WorkspaceProperties workspaceProperties;

    private final WorkspaceResourceHandler handler;

    @PostConstruct
    public void initialize() {
        if (!hasText(workspaceProperties.getApiUrl())) {
            log.error("Profile 'studio' is activated but studio API url not set or empty ! No synchronization will be performed between Studio and UID");
        }
    }

    @After("execution(* org.bonitasoft.web.designer.repository.Repository+.updateLastUpdateAndSave(..)) ")
    public void postSaveAndUpdateDate(JoinPoint joinPoint) {
        try {
            handler.postSave(filePath(joinPoint));
        } catch (ResourceNotFoundException e) {
            throw new RepositoryException("An error occured while proceeding post save action.", e);
        }
    }

    @After("execution(* org.bonitasoft.web.designer.repository.Repository+.save(..))")
    public void postSave(JoinPoint joinPoint) {
        postSaveAndUpdateDate(joinPoint);
    }

    @Around("execution(* org.bonitasoft.web.designer.repository.Repository+.delete(String))")
    public void delete(JoinPoint joinPoint) {
        try {
            Identifiable component = get(joinPoint);
            handler.delete(filePath(joinPoint));
            getPersister(joinPoint).delete(resolvePathFolder(joinPoint), component);
        } catch (ResourceNotFoundException | IOException e) {
            throw new RepositoryException("An error occured while proceeding delete action.", e);
        }
        try {
            handler.postDelete(filePath(joinPoint));
        } catch (ResourceNotFoundException e) {
            throw new RepositoryException("An error occured while proceeding post delete action.", e);
        }
    }

    private Path filePath(JoinPoint joinPoint) {
        return ((Repository<?>) joinPoint.getThis()).resolvePath(artifactId(joinPoint));
    }

    private Path resolvePathFolder(JoinPoint joinPoint) {
        return ((Repository<?>) joinPoint.getThis()).resolvePathFolder(artifactId(joinPoint));
    }

    private Identifiable get(JoinPoint joinPoint) {
        return ((Repository<?>) joinPoint.getThis()).get(artifactId(joinPoint));
    }

    private JsonFileBasedPersister getPersister(JoinPoint joinPoint) {
        return ((AbstractRepository) joinPoint.getThis()).getPersister();
    }

    private String artifactId(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length == 1) {
            Object argValue = args[0];
            //Passed argument is the id
            if (argValue instanceof String) {
                return (String) argValue;
            }
            //Passed argument is the element
            if (argValue instanceof Identifiable) {
                return ((Identifiable) argValue).getId();
            }
        }
        throw new IllegalArgumentException("Failed to retrieve element id for " + joinPoint.getSignature() + " call.");
    }

}
