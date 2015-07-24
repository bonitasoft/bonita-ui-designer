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
(function() {

  'use strict';

  angular.module('bonitasoft.designer.filters', []);
  angular.module('bonitasoft.designer.controllers', ['bonitasoft.designer.filters']);
  angular.module('bonitasoft.designer.services', ['bonitasoft.designer.filters', 'gettext']);
  angular.module('bonitasoft.designer.factories', ['bonitasoft.designer.filters']);
  angular.module('bonitasoft.designer.directives', ['bonitasoft.designer.filters']);
  angular.module('bonitasoft.designer.widgets', []);

  angular.module('bonitasoft.designer', [
    'app.route',
    'bonitasoft.designer.preview',
    'bonitasoft.designer.home',
    'bonitasoft.designer.custom-widget',
    'bonitasoft.designer.common.repositories',
    'bonitasoft.designer.common.services',
    'bonitasoft.designer.common.directives',
    'bonitasoft.designer.controllers',
    'bonitasoft.designer.factories',
    'bonitasoft.designer.services',
    'bonitasoft.designer.directives',
    'bonitasoft.designer.filters',
    'bonitasoft.designer.templates',
    'bonitasoft.designer.assets',
    'ngSanitize',
    'ngAnimate',
    'ui.router',
    'RecursionHelper',
    'ui.bootstrap',
    'ui.ace',
    'bonitasoft.designer.widgets',
    'org.bonitasoft.dragAndDrop',
    'gettext',
    'ngUpload',
    'angularMoment'
  ]);
})();
