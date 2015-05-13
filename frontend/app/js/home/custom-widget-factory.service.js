/**
 * Create a default custom widget.
 * TODO: should be moved to backend side
 */
angular.module('pb.home').factory('customWidgetFactory', function() {

  function createCustomWidget (name) {
    var template = '<div ng-click="ctrl.sayClicked()">Enter your template here, using {{ properties.value }}</div>';
    var controller = 'function Widget' + name + 'Controller($scope) {\n' +
      '    this.sayClicked = function() {\n' +
      '        $scope.properties.value = \'clicked\';\n' +
      '    };\n' +
      '}';

    return {
      name: name,
      template: template,
      controller: controller,
      custom: true,
      properties: [
        {
          label: 'Value',
          name: 'value',
          type: 'text',
          defaultValue: 'This is the initial value'
        }
      ]
    };
  }

  return {
    createCustomWidget: createCustomWidget
  };
});
