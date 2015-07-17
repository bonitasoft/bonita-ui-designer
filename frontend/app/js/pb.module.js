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

  angular.module('bonitasoft.ui.filters', []);
  angular.module('bonitasoft.ui.controllers', ['bonitasoft.ui.filters']);
  angular.module('bonitasoft.ui.services', ['bonitasoft.ui.filters', 'gettext']);
  angular.module('bonitasoft.ui.factories', ['bonitasoft.ui.filters']);
  angular.module('bonitasoft.ui.directives', ['bonitasoft.ui.filters']);
  angular.module('bonitasoft.ui.widgets', []);

  angular.module('bonitasoft.ui', [
    'app.route',
    'bonitasoft.ui.preview',
    'bonitasoft.ui.home',
    'bonitasoft.ui.custom-widget',
    'bonitasoft.ui.common.repositories',
    'bonitasoft.ui.common.services',
    'bonitasoft.ui.common.directives',
    'bonitasoft.ui.controllers',
    'bonitasoft.ui.factories',
    'bonitasoft.ui.services',
    'bonitasoft.ui.directives',
    'bonitasoft.ui.filters',
    'bonitasoft.ui.templates',
    'bonitasoft.ui.assets',
    'ngSanitize',
    'ngAnimate',
    'ui.router',
    'RecursionHelper',
    'ui.bootstrap',
    'ui.ace',
    'bonitasoft.ui.widgets',
    'org.bonitasoft.dragAndDrop',
    'gettext',
    'ngUpload',
    'angularMoment'
  ]);
})();
