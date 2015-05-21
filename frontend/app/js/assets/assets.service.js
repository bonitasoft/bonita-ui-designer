(function() {

  angular.module('pb.assets').service('assets', function () {

      'use strict';

      var types = [
        { key: 'js', label: 'JavaScript'},
        { key: 'css', label: 'CSS'},
        { key: 'img', label: 'Images'}
      ];

      return {
        initFilterMap: function() {
          return types.map(function(obj){
            obj.filter = true;
            return obj;
          });
        },

        getLabel: function(key) {
          var type = types.filter(function(element){
            return element.key === key;
          })[0];

          return type ? type.label : '';
        }
      };
    });

})();