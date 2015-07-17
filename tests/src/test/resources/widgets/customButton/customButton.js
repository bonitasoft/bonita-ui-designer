angular.module('bonitasoft.ui.widgets')
  .directive('customButton', function() {
    return {
      controllerAs: 'ctrl',
      controller: function WidgetbuttonController($scope) {
    this.sayClicked = function() {
        $scope.properties.value = 'clicked';
    };
},
      template: '<button ng-click="ctrl.sayClicked()">Enter your template here, using {{ properties.value }}</button>'
    };
  });
