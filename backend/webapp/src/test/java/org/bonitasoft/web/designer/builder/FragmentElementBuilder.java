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
package org.bonitasoft.web.designer.builder;

import org.bonitasoft.web.designer.model.page.FragmentElement;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Colin Puy
 */
public class FragmentElementBuilder extends ElementBuilder<FragmentElement> {

    private FragmentElement element = new FragmentElement();

    private Map<String, String> bindings = new HashMap<>();

    public static FragmentElementBuilder aFragmentElement() {
        return new FragmentElementBuilder();
    }

    public FragmentElementBuilder withFragmentId(String id) {
        element.setId(id);
        return this;
    }

    public FragmentElementBuilder withBinding(String name, String value) {
        bindings.put(name, value);
        element.setBinding(bindings);
        return this;
    }

    @Override
    protected FragmentElement getElement() {
        return element;
    }

    @Override
    public FragmentElement build() {
        return element;
    }

}
