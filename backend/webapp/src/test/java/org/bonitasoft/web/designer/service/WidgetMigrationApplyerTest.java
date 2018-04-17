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
package org.bonitasoft.web.designer.service;

import static org.mockito.Mockito.mock;

import java.util.Collections;

import org.bonitasoft.web.designer.builder.WidgetBuilder;
import org.bonitasoft.web.designer.migration.Migration;
import org.bonitasoft.web.designer.migration.MigrationStep;
import org.bonitasoft.web.designer.model.widget.Widget;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class WidgetMigrationApplyerTest {

    @Test
    public void should_migrate_a_widget(){
        Migration<Widget> migrations = new Migration("1.2.0", mock(MigrationStep.class));
        WidgetMigrationApplyer widgetMigrationApplyer = new WidgetMigrationApplyer(Collections.singletonList(migrations));
        Widget widget = WidgetBuilder.aWidget().id("customWidget").version("1.0.1").previousDesignerVersion("1.0.1").build();

        widgetMigrationApplyer.migrate(widget);

        Assert.assertEquals(widget.getPreviousDesignerVersion(),"1.0.1");
        Assert.assertEquals(widget.getDesignerVersion(),"1.2.0");
        Assert.assertEquals(widget.getDesignerVersion(),"1.2.0");
    }

    @Test
    public void should_not_modify_previous_designer_version_when_no_migration_done_on_widget(){
        Migration<Widget> migrations = new Migration("1.0.2", mock(MigrationStep.class));
        WidgetMigrationApplyer widgetMigrationApplyer = new WidgetMigrationApplyer(Collections.singletonList(migrations));
        Widget widget = WidgetBuilder.aWidget().id("customWidget").version("1.0.2").previousDesignerVersion("1.0.2").build();

        widgetMigrationApplyer.migrate(widget);

        Assert.assertEquals(widget.getPreviousDesignerVersion(),"1.0.2");
        Assert.assertEquals(widget.getDesignerVersion(),"1.0.2");
    }

}
