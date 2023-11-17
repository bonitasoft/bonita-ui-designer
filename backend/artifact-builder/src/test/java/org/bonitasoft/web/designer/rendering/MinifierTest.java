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
package org.bonitasoft.web.designer.rendering;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Benjamin Parisel
 */
public class MinifierTest {

    @Test
    public void should_minify_input() throws IOException {
        String content = "function PbInputCtrl($scope, $log, widgetNameFactory) {\n" +"\n'use strict';\n" +
                "\n  this.name = widgetNameFactory.getName('pbInput');\n" +
                "\n  var myString = `http://some-url.com ${name} other`;\n" +
                "\n  if (!$scope.properties.isBound('value')) {\n" +
                "    $log.error('the pbInput property named \"value\" need to be bound to a variable');\n" +
                "  }\n}\n";
        String expected = "\n" +
                "function PbInputCtrl($scope,$log,widgetNameFactory){'use strict';this.name=widgetNameFactory.getName" +
                "('pbInput');var myString=`http://some-url.com ${name} other`;if(!$scope.properties.isBound('value')){$log.error('the pbInput property named \"value\"" +
                " need to be bound to a variable');}}";

        byte[] min = Minifier.minify(content.getBytes());

        assertThat(new String(min,"UTF-8")).isEqualTo(expected);
    }


    @Test(expected = GenerationException.class)
    public void should_throw_exeption_when_content_have_bad_unterminated_comment() throws IOException {
        String badCommentTemplate ="/** dffsf /";
        String content = badCommentTemplate + "content";

        Minifier.minify(content.getBytes());
    }

    @Test(expected = GenerationException.class)
    public void should_throw_exeption_when_content_have_bad_unterminated_string() throws IOException {
        String unterminatedString = "'use strict\n";
        String content = "function PbInputCtrl($scope, $log, widgetNameFactory) { \n" + unterminatedString;

        Minifier.minify(content.getBytes());
    }
}
