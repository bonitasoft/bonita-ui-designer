/**
 * Repo containing all our palette widgets.
 */
angular.module('pb.common.services')
  .provider('resolutions', function () {

    'use strict';

    var resolutions = [
      {
        key: 'xs',
        icon: 'laptop',
        width: 320
      }
    ];

    var defaultResolution = resolutions[0];
    var currentResolution = resolutions[0];

    var defaultDimension = {
      xs: 12
    };

    return {
      registerResolutions: function(newResolutions) {
         resolutions =  newResolutions;
      },

      setDefaultResolution: function(resolutionKey) {
        defaultResolution =  get(resolutionKey);
        currentResolution =  defaultResolution;
      },

      setDefaultDimension: function(dimension) {
        defaultDimension = dimension || defaultDimension;
      },

      $get: function() {
        return {
          all: all,
          get: get,
          selected: selected,
          select: select,
          getDefaultResolution: getDefaultResolution,
          getDefaultDimension: getDefaultDimension
        };
      }
    };

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
    function get(resolutionKey){
      return resolutions.filter(function(resolution){
        return resolution.key === resolutionKey;
      })[0] || defaultResolution;
    }


  });
