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
package org.bonitasoft.web.designer.workspace;


import static com.google.common.collect.Lists.newArrayList;
import static org.bonitasoft.web.designer.builder.AssetBuilder.anAsset;
import static org.bonitasoft.web.designer.builder.PageBuilder.aPage;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.bonitasoft.web.designer.model.asset.Asset;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.repository.PageRepository;
import org.bonitasoft.web.designer.repository.WidgetRepository;
import org.bonitasoft.web.designer.retrocompatibility.ComponentMigrator;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class WorkspaceMigratorTest {
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();
    @Mock
    private PageRepository pageRepository;
    @Mock
    private WidgetRepository widgetRepository;
    @Mock
    private ComponentMigrator componentMigrator;
    @InjectMocks
    private WorkspaceMigrator workspaceMigrator;

    @Test
    public void should_migrate_page_create_in_another_version_with_asset_id_null() {
        Asset asset = anAsset().withId(null).build();
        Page page = aPage().withVersion("1.0.0-SNAPSHOT").withAsset(asset).build();
        when(pageRepository.getAll()).thenReturn(newArrayList(page));

        workspaceMigrator.migrate();

        verify(componentMigrator).migrate(pageRepository, page);
    }
}