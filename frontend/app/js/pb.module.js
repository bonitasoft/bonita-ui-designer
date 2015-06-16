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

  angular.module('pb.filters', []);
  angular.module('pb.controllers', ['pb.filters']);
  angular.module('pb.services', ['pb.filters', 'gettext']);
  angular.module('pb.factories', ['pb.filters']);
  angular.module('pb.directives', ['pb.filters']);
  angular.module('pb.widgets', []);

  angular.module('pb', [
    'app.route',
    'pb.preview',
    'pb.home',
    'pb.custom-widget',
    'pb.common.repositories',
    'pb.common.services',
    'pb.common.directives',
    'pb.controllers',
    'pb.factories',
    'pb.services',
    'pb.directives',
    'pb.filters',
    'pb.templates',
    'pb.assets',
    'ngSanitize',
    'ui.router',
    'RecursionHelper',
    'ui.bootstrap',
    'ui.ace',
    'pb.widgets',
    'org.bonitasoft.dragAndDrop',
    'gettext',
    'ngUpload',
    'angularMoment'
  ]);
})();
