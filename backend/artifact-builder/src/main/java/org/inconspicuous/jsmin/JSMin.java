/** 
 * Copyright (C) 2023 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package inconspicuous.jsmin;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Copyright (c) 2006 John Reilly (www.inconspicuous.org) This work is a
 * translation from C to Java of jsmin.c published by Douglas Crockford.
 * Permission is hereby granted to use the Java version under the same
 * conditions as the jsmin.c on which it is based.
 * <p>
 * http://www.crockford.com/javascript/jsmin.html
 */
public class JSMin {

    private static final int EOF = -1;

    private Reader in;
    private Writer out;

    private int the_a;
    private int the_b;
    private int look_ahead = EOF;
    private int the_x = EOF;
    private int the_y = EOF;

    /**
     * Performs minification of the given input script.
     *
     * @param script the source script
     * @return the minified script
     * @throws JSMin.MinifyException
     */
    public static String minify(String script) throws JSMin.MinifyException {
        StringReader in = new StringReader(script);
        StringWriter out = new StringWriter();
        minify(in, out);
        return out.toString();
    }

    /**
     * Performs minification of the input read from in and writes the minified
     * script to out.
     *
     * @param in the source script read
     * @param out the minified script writer
     * @throws JSMin.MinifyException
     */
    public static void minify(Reader in, Writer out) throws JSMin.MinifyException {
        try {
            JSMin min = new JSMin(in, out);
            min.apply();
        } catch (IOException e) {
            error("Failed to read/write input: " + e.getMessage());
        }
    }

    /**
     * Throws a runtime exception with the given message.
     * 
     * @param message the message
     * @throws JSMin.MinifyException
     */
    private static void error(String message) throws JSMin.MinifyException {
        throw new MinifyException("JSMIN error: " + message);
    }

    /**
     * Private constructor of the JSMin class
     */
    private JSMin(Reader in, Writer out) throws IOException {
        this.in = in;
        this.out = out;
    }

    /**
     * is_alphanum -- return true if the character is a letter, digit, underscore,
     * dollar sign, or non-ASCII character.
     */
    private boolean is_alphanum(int codeunit) {
        return (((codeunit >= 'a') && (codeunit <= 'z')) || ((codeunit >= '0') && (codeunit <= '9'))
                || ((codeunit >= 'A') && (codeunit <= 'Z')) || (codeunit == '_') || (codeunit == '$')
                || (codeunit == '\\') || (codeunit > 126));
    }

    /**
     * get -- return the next character from stdin. Watch out for lookahead. If the
     * character is a control character, translate it to a space or linefeed.
     */
    private int get() throws IOException {
        int codeunit = this.look_ahead;
        this.look_ahead = EOF;
        if (codeunit == EOF) {
            codeunit = this.in.read();
        }
        if ((codeunit >= ' ') || (codeunit == '\n') || (codeunit == EOF)) {
            return codeunit;
        }
        if (codeunit == '\r') {
            return '\n';
        }
        return ' ';
    }

    /**
     * peek -- get the next character without advancing.
     */
    private int peek() throws IOException {
        this.look_ahead = this.get();
        return this.look_ahead;
    }

    /**
     * next -- get the next character, excluding comments. peek() is used to see if
     * a '/' is followed by a '/' or '*'.
     * 
     * @throws JSMin.MinifyException
     */
    private int next() throws IOException, JSMin.MinifyException {
        int codeunit = this.get();
        if (codeunit == '/') {
            switch (this.peek()) {
                case '/':
                    for (;;) {
                        codeunit = this.get();
                        if (codeunit <= '\n') {
                            break;
                        }
                    }
                    break;
                case '*':
                    this.get();
                    while (codeunit != ' ') {
                        switch (this.get()) {
                            case '*':
                                if (this.peek() == '/') {
                                    this.get();
                                    codeunit = ' ';
                                }
                                break;
                            case EOF:
                                error("Unterminated comment.");
                        }
                    }
                    break;
            }
        }
        this.the_y = this.the_x;
        this.the_x = codeunit;
        return codeunit;
    }

    /**
     * action -- do something! What you do is determined by the argument: 1 Output
     * A. Copy B to A. Get the next B. 2 Copy B to A. Get the next B. (Delete A). 3
     * Get the next B. (Delete B). action treats a string as a single character.
     * action recognizes a regular expression if it is preceded by the likes of '('
     * or ',' or '='.
     * 
     * @throws JSMin.MinifyException
     */
    private void action(int determined) throws IOException, JSMin.MinifyException {
        switch (determined) {
            case 1:
                this.out.write(this.the_a);
                if (((this.the_y == '\n') || (this.the_y == ' '))
                        && ((this.the_a == '+') || (this.the_a == '-') || (this.the_a == '*') || (this.the_a == '/'))
                        && ((this.the_b == '+') || (this.the_b == '-') || (this.the_b == '*') || (this.the_b == '/'))) {
                    this.out.write(this.the_y);
                }
            case 2:
                this.the_a = this.the_b;
                if ((this.the_a == '\'') || (this.the_a == '"') || (this.the_a == '`')) {
                    for (;;) {
                        this.out.write(this.the_a);
                        this.the_a = this.get();
                        if (this.the_a == this.the_b) {
                            break;
                        }
                        if (this.the_a == '\\') {
                            this.out.write(this.the_a);
                            this.the_a = this.get();
                        }
                        if (this.the_a == EOF) {
                            error("Unterminated string literal.");
                        }
                    }
                }
            case 3:
                this.the_b = this.next();
                if ((this.the_b == '/') && ((this.the_a == '(') || (this.the_a == ',') || (this.the_a == '=')
                        || (this.the_a == ':') || (this.the_a == '[') || (this.the_a == '!') || (this.the_a == '&')
                        || (this.the_a == '|') || (this.the_a == '?') || (this.the_a == '+') || (this.the_a == '-')
                        || (this.the_a == '~') || (this.the_a == '*') || (this.the_a == '/') || (this.the_a == '{')
                        || (this.the_a == '}') || (this.the_a == ';'))) {
                    this.out.write(this.the_a);
                    if ((this.the_a == '/') || (this.the_a == '*')) {
                        this.out.write(' ');
                    }
                    this.out.write(this.the_b);
                    for (;;) {
                        this.the_a = this.get();
                        if (this.the_a == '[') {
                            for (;;) {
                                this.out.write(this.the_a);
                                this.the_a = this.get();
                                if (this.the_a == ']') {
                                    break;
                                }
                                if (this.the_a == '\\') {
                                    this.out.write(this.the_a);
                                    this.the_a = this.get();
                                }
                                if (this.the_a == EOF) {
                                    error("Unterminated set in Regular Expression literal.");
                                }
                            }
                        } else if (this.the_a == '/') {
                            switch (this.peek()) {
                                case '/':
                                case '*':
                                    error("Unterminated set in Regular Expression literal.");
                            }
                            break;
                        } else if (this.the_a == '\\') {
                            this.out.write(this.the_a);
                            this.the_a = this.get();
                        }
                        if (this.the_a == EOF) {
                            error("Unterminated Regular Expression literal.");
                        }
                        this.out.write(this.the_a);
                    }
                    this.the_b = this.next();
                }
        }
    }

    /**
     * jsmin -- Copy the input to the output, deleting the characters which are
     * insignificant to JavaScript. Comments will be removed. Tabs will be replaced
     * with spaces. Carriage returns will be replaced with linefeeds. Most spaces
     * and linefeeds will be removed.
     * 
     * @throws JSMin.MinifyException
     */
    public void apply() throws IOException, JSMin.MinifyException {

        if (this.peek() == 0xEF) {
            this.get();
            this.get();
            this.get();
        }

        this.the_a = '\n';
        this.action(3);
        while (this.the_a != EOF) {
            switch (this.the_a) {
                case ' ':
                    this.action(this.is_alphanum(this.the_b) ? 1 : 2);
                    break;
                case '\n':
                    switch (this.the_b) {
                        case '{':
                        case '[':
                        case '(':
                        case '+':
                        case '-':
                        case '!':
                        case '~':
                            this.action(1);
                            break;
                        case ' ':
                            this.action(3);
                            break;
                        default:
                            this.action(this.is_alphanum(this.the_b) ? 1 : 2);
                    }
                    break;
                default:
                    switch (this.the_b) {
                        case ' ':
                            this.action(this.is_alphanum(this.the_a) ? 1 : 3);
                            break;
                        case '\n':
                            switch (this.the_a) {
                                case '}':
                                case ']':
                                case ')':
                                case '+':
                                case '-':
                                case '"':
                                case '\'':
                                case '`':
                                    this.action(1);
                                    break;
                                default:
                                    this.action(this.is_alphanum(this.the_a) ? 1 : 3);
                            }
                            break;
                        default:
                            this.action(1);
                            break;
                    }
            }
        }
    }

    public static class MinifyException extends Exception {

        public MinifyException(String message) {
            super(message);
        }
    }

}
