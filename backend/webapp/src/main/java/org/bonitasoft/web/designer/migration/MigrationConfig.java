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

import org.bonitasoft.web.designer.model.fragment.Fragment;
import org.bonitasoft.web.designer.model.page.Page;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.bonitasoft.web.designer.common.repository.FragmentRepository;
import org.bonitasoft.web.designer.common.repository.PageRepository;
import org.bonitasoft.web.designer.common.repository.WidgetRepository;
import org.bonitasoft.web.designer.UiDesignerCore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MigrationConfig {


    @Bean
    public LiveRepositoryUpdate<Page> pageRepositoryLiveUpdate(PageRepository pageRepository, UiDesignerCore uiDesignerCore) {
        return new LiveRepositoryUpdate<>(pageRepository, uiDesignerCore.getPageMigrationStepsList());
    }

    @Bean
    public LiveRepositoryUpdate<Fragment> fragmentLiveRepositoryUpdate(FragmentRepository fragmentRepository, UiDesignerCore uiDesignerCore) {
        return new LiveRepositoryUpdate<>(fragmentRepository, uiDesignerCore.getFragmentMigrationStepsList());
    }

    @Bean
    public LiveRepositoryUpdate<Widget> widgetLiveRepositoryUpdate(WidgetRepository widgetRepository, UiDesignerCore uiDesignerCore) {
        return new LiveRepositoryUpdate<>(widgetRepository, uiDesignerCore.getWidgetMigrationStepsList());
    }


}
