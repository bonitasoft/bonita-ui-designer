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
package org.bonitasoft.web.designer.utils.rule;

import org.apache.commons.io.IOUtils;
import org.junit.rules.ExternalResource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class TestResource extends ExternalResource {

    String packageName;
    InputStream stream;

    public TestResource(Class<?> aClass) {
        this.packageName = aClass.getPackage().getName();
    }

    public String load(String fileName) {
        String filePath = "/" + packageName.replace(".", "/") + "/" + fileName;
        try {
            stream = getClass().getResourceAsStream(filePath);
            if (stream == null) {
                throw new Error(String.format("Unable to load test resource %s", filePath));
            }
            return IOUtils.toString(stream, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new Error(String.format("Unable to load test resource %s", filePath), e);
        }
    }

    @Override
    protected void before() throws Throwable {
        super.before();
        stream = getClass().getResourceAsStream(packageName);
    }

    @Override
    protected void after() {
        IOUtils.closeQuietly(stream);
        super.after();
    }

}
