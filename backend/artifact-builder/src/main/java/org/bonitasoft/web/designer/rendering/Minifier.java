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

import org.inconspicuous.jsmin.JSMin;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author Benjamin Parisel
 */
public final class Minifier {

    public static byte[] minify(byte[] contentToMinify) {
        try (var bis = new ByteArrayInputStream(contentToMinify); var out = new ByteArrayOutputStream()) {
            var jsmin = new JSMin(bis, out);
            jsmin.jsmin();
            return out.toByteArray();
        } catch (IOException e) {
            throw new GenerationException("Error when minify", e);
        } catch (JSMin.UnterminatedCommentException e) {
            throw new GenerationException("Error when minify: Unterminated Comment", e);
        } catch (JSMin.UnterminatedStringLiteralException e) {
            throw new GenerationException("Error when minify: Unterminated String", e);
        } catch (JSMin.UnterminatedRegExpLiteralException e) {
            throw new GenerationException("Error when minify: Unterminated RegExp literal", e);
        }
    }
}
