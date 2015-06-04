angular.module('pb.widgets')
  .directive('pbLabel', function() {
    return {
      template: '<div class="text-{{ properties.alignment }}"><label>{{ properties.text }}</label></div>\n'
    };
  });
