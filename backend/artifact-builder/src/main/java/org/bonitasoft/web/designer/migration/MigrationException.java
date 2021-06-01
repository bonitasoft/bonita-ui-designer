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
package org.bonitasoft.web.designer.migration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MigrationException extends Exception {

    private static final Logger logger = LoggerFactory.getLogger(MigrationException.class);
    private String message;
    private String className;

    public MigrationException(Throwable e, String message) {
        super(e);
        this.message = message;
        logger.error(message, e);
    }

    public MigrationException(Throwable e, String message, String className) {
        super(e);
        this.message = message;
        this.className = className;
        logger.error(message, e);
    }

    public MigrationException(Throwable e) {
        super(e);
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
