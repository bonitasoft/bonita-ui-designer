/**
 * Create a default custom widget.
 * TODO: should be moved to backend side
 */
angular.module('pb.home').factory('customWidgetFactory', function() {

  function createCustomWidget (name) {
    var template = '<!-- Custom widget template is defined here.\n   - You can use standard html tags and AngularJS built in directives, scope and interpolation system\n   - Custom widget properties created on the right panel can be used as variables in templates with properties.newProperty\n   - Functions exposed in controller can be used with ctrl.newFunction()\n -->\n \n<div style=\"color: {{ properties.color }}; background-color: {{ backgroudColor }}\" ng-click=\"ctrl.toggleBackgroundColor()\">\n    Value is:  <i>{{ properties.value }}</i>. Click me to toggle background color\n</div>';
    var controller = '/**\n * Controller is a javascript function that augment the AngularJS scope and expose functions that can be used in custom widget template.\n * \n * Custom widget properties created on the right panel can be used as variables in controller with $scope.properties.\n * To use AngularJS standard services you must declare them into the main function arguments. \n * \n * You can leave the controller empty if you don\'t need it.\n */\nfunction ($scope) {\n    var white = \'white\';\n    \n    // add a new variable in AngularJS scope. It\'ll be usable in the template directly with {{ backgroudColor }} \n    $scope.backgroudColor = white;\n    \n    // define a function to be used in template with ctrl.toggleBackgroundColor()\n    this.toggleBackgroundColor = function() {\n        if ($scope.backgroudColor === white) {\n           // use the custom widget property backgroudColor with $scope.properties.backgroudColor\n            $scope.backgroudColor = $scope.properties.background;\n        } else {\n            $scope.backgroudColor = white;\n        }\n    };\n}';

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
        },
        {
          label: 'Color',
          name: 'color',
          type: 'choice',
          defaultValue: 'RebeccaPurple',
          choiceValues: [
            'RebeccaPurple',
            'Chartreuse',
            'Tomato',
            'DeepSkyBlue'
          ],
          bidirectional: false
        },
        {
          label: 'Background color on click',
          name: 'background',
          type: 'choice',
          defaultValue: 'Yellow',
          choiceValues: [
            'Yellow',
            'LightGray'
          ],
          bidirectional: false
        }
      ]
    };
  }

  return {
    createCustomWidget: createCustomWidget
  };
});
