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
package org.bonitasoft.web.designer.controller.importer.dependencies;

import org.bonitasoft.web.designer.model.Identifiable;
import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.repository.FragmentRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FragmentDependencyImporter extends ComponentDependencyImporter<Fragment> {

    private final FragmentRepository fragmentRepository;

    public FragmentDependencyImporter(FragmentRepository fragmentRepository) {
        super(fragmentRepository);
        this.fragmentRepository = fragmentRepository;
    }

    @Override
    public List<Fragment> load(Identifiable parent, Path resources) throws IOException {
        var fragmentsPath = resources.resolve("fragments");
        if (Files.exists(fragmentsPath)) {
            return fragmentRepository.loadAll(fragmentsPath);
        }
        return new ArrayList<>();
    }

    @Override
    public void save(List<Fragment> elements, Path resources) {
        fragmentRepository.saveAll(elements);
    }

}
