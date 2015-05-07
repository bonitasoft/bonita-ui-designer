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
package org.bonitasoft.web.designer.controller.utils;

import static junitparams.JUnitParamsRunner.$;
import static org.assertj.core.api.Assertions.assertThat;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class MimeTypeTest {

    /**
     * Values injected in the test.
     * <ul>
     * <li>First value is our enum mime type (example MimeType.APPLICATION_ZIP)</li>
     * <li>Second value is value of the mime type sent by the web client (example "application/zip")</li>
     * <li>Last value is the expected result</li>
     * </ul>
     */
    protected Object[] typeMimeValues() {
        return $(
                $(MimeType.APPLICATION_ZIP, "application/zip", true),
                $(MimeType.APPLICATION_ZIP, "application/x-zip", true),
                $(MimeType.APPLICATION_ZIP, "application/not-a-zip", false)
        );
    }

    @Parameters(method = "typeMimeValues")
    @Test
    public void should_verify_main_mimeType(MimeType mimeType, String mimeTypeText, boolean resultExpected) throws Exception {
        boolean matches = mimeType.matches(mimeTypeText);
        assertThat(matches).isEqualTo(resultExpected);
    }
}
