/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 31 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.web.designer.config;

import static java.nio.file.Paths.get;

import java.net.URISyntaxException;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotatedTypeMetadata;


@Order(Ordered.HIGHEST_PRECEDENCE)
public class DesignerConfigConditional implements Condition {

    private static final Logger logger = LoggerFactory.getLogger(DesignerConfigConditional.class);

    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
        try{
            get(DesignerConfigConditional.class.getClassLoader().getResource("org/bonitasoft/web/designer/config/DesignerConfigSP.class").toURI());
            logger.info("UI-DESIGNER :  SP edition");
            logger.info(Strings.repeat("=", 100));
            return false;
        } catch (URISyntaxException | NullPointerException e) {
            logger.info("UI-DESIGNER :  Community edition");
            logger.info(Strings.repeat("=", 100));
            return true;
        }
    }
}
