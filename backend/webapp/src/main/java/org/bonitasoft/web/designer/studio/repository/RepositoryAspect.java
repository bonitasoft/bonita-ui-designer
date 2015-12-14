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

import java.nio.file.Path;
import javax.inject.Inject;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.repository.Repository;
import org.bonitasoft.web.designer.repository.exception.RepositoryException;
import org.bonitasoft.web.designer.studio.workspace.LockedResourceException;
import org.bonitasoft.web.designer.studio.workspace.ResourceNotFoundException;
import org.bonitasoft.web.designer.studio.workspace.WorkspaceResourceHandler;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Use environment variable -Dspring_profiles_active=studio to activate this aspect
 *
 * @author Romain Bioteau
 */
@Profile("studio")
@Aspect
@Component
public class RepositoryAspect {

    private WorkspaceResourceHandler handler;

    @Inject
    public RepositoryAspect(WorkspaceResourceHandler handler) {
        this.handler = handler;
    }

    /*
     * BS-14120
     * Useless numerous notfications may lead to a localized studio crash,
     * we decided to deactivate them until we find a proper solution for
     * workspace management shared between studio and UIDesigner
     */
    //@Before("execution(* org.bonitasoft.web.designer.repository.Repository+.get(String))")
    public void preOpen(JoinPoint joinPoint) {
        try {
            handler.preOpen(filePath(joinPoint));
        } catch (ResourceNotFoundException | LockedResourceException e) {
            throw new RepositoryException("An error occured while proceeding pre open action.", e);
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

    @After("execution(* org.bonitasoft.web.designer.repository.Repository+.delete(String))")
    public void postDelete(JoinPoint joinPoint) {
        try {
            handler.postDelete(filePath(joinPoint));
        } catch (ResourceNotFoundException e) {
            throw new RepositoryException("An error occured while proceeding post delete action.", e);
        }
    }

    private Path filePath(JoinPoint joinPoint) {
        return ((Repository<?>) joinPoint.getThis()).resolvePath(artifactId(joinPoint));
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
