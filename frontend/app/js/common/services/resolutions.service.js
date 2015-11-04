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
    .module('bonitasoft.designer.common.services')
    .service('resolutions', resolutionsService);

  function resolutionsService() {

    var defaultResolution = { key: 'xs', icon: 'laptop', width: '100%' }, currentResolution = defaultResolution;

    var resolutions = [defaultResolution];

    var defaultDimension = {
      xs: 12
    };

    return {
      registerResolutions: registerResolutions,
      setDefaultResolution: setDefaultResolution,
      setDefaultDimension: setDefaultDimension,
      all: all,
      get: get,
      selected: selected,
      select: select,
      getDefaultResolution: getDefaultResolution,
      getDefaultDimension: getDefaultDimension
    };

    function registerResolutions(newResolutions) {
      resolutions = newResolutions;
    }

    function setDefaultResolution(resolutionKey) {
      defaultResolution = get(resolutionKey);
      currentResolution = defaultResolution;
    }

    function setDefaultDimension(dimension) {
      defaultDimension = dimension || defaultDimension;
    }

    /**
     * Returns the default dimension object
     * @returns {Object}
     */
    function getDefaultDimension() {
      return angular.copy(defaultDimension);
    }

    /**
     * Returns the default resolution
     * @returns {Object}
     */
    function getDefaultResolution() {
      return defaultResolution;
    }

    /**
     * Returns all resolutions ordered by size
     * @returns {Array}
     */
    function all() {
      return resolutions;
    }

    /**
     * Return the selected resolution from the URL param
     * @return {Object}
     */
    function selected() {
      return currentResolution;
    }

    /**
     * select a resolution
     * @param key key of the resolution to be selected
     */
    function select(key) {
      currentResolution = get(key);
      return currentResolution;
    }

    /**
     * Returns a resolution by its key or the default resolution if a resolution is not found.
     * @param {String} resolutionKey - the resolution key to look for
     * @returns {Object}
     */
    function get(resolutionKey) {
      return resolutions.filter((resolution) => resolution.key === resolutionKey).pop() || defaultResolution;
    }

  }
})();
