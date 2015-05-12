/**
 * Repo containing all our palette widgets.
 */
angular.module('pb.common.services')
  .provider('resolutions', function () {

    'use strict';

    /**
     * The various resolutions that the user can choose.
     */
    var resolutions = [
      {
        key: 'xs',
        label: 'Phone',
        icon: 'mobile',
        width: 320,
        tooltip: 'Extra Small devices (width < 768px)'
      }
    ];

    var defaultResolution = resolutions[0];
    var currentResolution = resolutions[0];

    return {
      addResolutions: function(newResolutions) {
         resolutions =  resolutions.concat(newResolutions);
      },

      setDefaultResolution: function(resolutionKey) {
        defaultResolution =  get(resolutionKey);
        currentResolution =  defaultResolution;
      },

      $get: function() {
        return {
          all: all,
          get: get,
          selected: selected,
          select: select,
          getDefaultResolution: getDefaultResolution
        };
      }
    };

    /**
     * Returns the default resolution, currently the desktop one.
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
