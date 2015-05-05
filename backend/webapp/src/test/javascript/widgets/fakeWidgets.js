angular.module('fakeWidgets', [])
  .directive('fakeInput',function() {
    return {
      restrict: 'E',
      controller: function($scope) {
      },
      template: '<input ng-model="properties.text" />'
    };
  }).directive('fakeLabel', function() {
    return {
      restrict: 'E',
      controller: function($scope) {
      },
      template: '<label>{{ properties.label }}</label>'
    };
  });
