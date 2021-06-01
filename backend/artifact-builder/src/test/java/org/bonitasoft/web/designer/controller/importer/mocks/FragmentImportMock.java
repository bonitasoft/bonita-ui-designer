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
package org.bonitasoft.web.designer.controller.importer.mocks;

import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.repository.FragmentRepository;
import org.bonitasoft.web.designer.repository.JsonFileBasedLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.bonitasoft.web.designer.builder.FragmentBuilder.aFragment;
import static org.mockito.Mockito.when;

public class FragmentImportMock {

    private static final String FRAGMENTS_FOLDER = "fragments";

    private FragmentRepository fragmentRepository;
    private JsonFileBasedLoader<Fragment> fragmentLoader;
    private Path unzippedPath;
    private List<Fragment> fragments = new ArrayList<>();

    public FragmentImportMock(Path unzippedPath, FragmentRepository fragmentRepository,
            JsonFileBasedLoader<Fragment> fragmentLoader) throws IOException {
        this.fragmentRepository = fragmentRepository;
        this.fragmentLoader = fragmentLoader;
        this.unzippedPath = unzippedPath;
    }

    public List<Fragment> mockFragmentsAsAddedDependencies() throws IOException {
        Files.createDirectories(unzippedPath.resolve(FRAGMENTS_FOLDER));
        List<Fragment> fragments = asList(
                aFragment().withId("aFragment").build(),
                aFragment().withId("anotherFragment").build());
        this.fragments.addAll(fragments);
        when(fragmentRepository.getComponentName()).thenReturn("fragment");
        when(fragmentLoader.getAll(unzippedPath.resolve(FRAGMENTS_FOLDER))).thenReturn(this.fragments);
        return fragments;
    }

    public List<Fragment> mockFragmentsAsOverriddenDependencies() throws IOException {
        Files.createDirectories(unzippedPath.resolve(FRAGMENTS_FOLDER));
        List<Fragment> fragments = asList(
                aFragment().withId("anExistingFragment").build(),
                aFragment().withId("anotherExistingFragment").build());
        this.fragments.addAll(fragments);
        when(fragmentRepository.getComponentName()).thenReturn("fragment");
        when(fragmentLoader.getAll(unzippedPath.resolve(FRAGMENTS_FOLDER))).thenReturn(this.fragments);
        when(fragmentRepository.exists("anotherExistingFragment")).thenReturn(true);
        when(fragmentRepository.exists("anExistingFragment")).thenReturn(true);
        return fragments;
    }

    public Fragment mockFragmentToBeImported() {
        Fragment fragment = aFragment().withId("aFragment").build();
        when(fragmentLoader.load(unzippedPath.resolve("fragment.json"))).thenReturn(fragment);
        return fragment;
    }

}
