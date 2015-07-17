angular.module('bonitasoft.ui.widgets')
  .directive('pbInput', function() {
    return {
      controllerAs: 'ctrl',
      controller: function(){$scope.hello = 'Hello'},
      template: '<div>{{ hello + \'there\'}}</div>'
    };
  });
