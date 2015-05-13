/**
 * Listen to an input file and assign the selected file to a scope variable
 */
angular.module('pb.home')
  .directive('fileInputChange', function(){

    'use strict';

    return {
      require: 'ngModel',
      link: function(scope, elem, attr, ngModel) {

        function update(event) {
          var filename = '';
          if (event.target.files && event.target.files.length > 0) {
            filename = event.target.files[0].name;
          } else {
            filename = event.target.value.match(/([^\\|\/]*)$/)[0] ;
          }

          scope.$apply(function(){
            ngModel.$setViewValue(filename);
            ngModel.$render();
          });
        }

        elem.on('change', update);

        scope.$on('$destroy', function() {
          elem.off('change', update);
        });

      }
    };
  });
