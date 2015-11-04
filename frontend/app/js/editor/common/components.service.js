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

  angular
    .module('bonitasoft.designer.editor.common')
    .factory('components', componentsService);

  function componentsService() {

    var componentsMap = {};

    return {
      reset: reset,
      register: register,
      init: init,
      get: get
    };

    function register(items) {
      componentsMap = items.reduce(function(components, item) {
        components[item.component.id] = item;
        return components;
      }, componentsMap);
    }

    function init(component, parentRow) {
      // container have no id, only a type
      var id = component.id || component.type;

      if (!componentsMap.hasOwnProperty(id)) {
        throw new Error('Component ' + id + ' has not been registered');
      }
      var fnInit = componentsMap[id].init;
      fnInit(component, parentRow);
    }

    function reset() {
      componentsMap = {};
    }

    function get() {
      return componentsMap;
    }
  }

})();
