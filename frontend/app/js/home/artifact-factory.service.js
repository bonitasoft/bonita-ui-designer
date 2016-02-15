/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * Factories to create new artifacts.
 * TODO: should be moved to backend side
 */
(function() {
  'use strict';

  class ArtifactFactories {
    constructor(gettext) {
      this.factories = {
        page: {
          key: 'page',
          state: 'page',
          value: gettext('Application page'),
          filterName: gettext('Pages'),
          create: name => this.createPage(name, 'page')
        },
        form: {
          key: 'form',
          state: 'page',
          value: gettext('Process form'),
          tooltip: gettext('You should start process form design in the studio: from the appropriate pool or human task, create a contract and launch the UI Designer. It will auto-generate a dedicated form to edit.'),
          filterName: gettext('Forms'),
          create: name => this.createPage(name, 'form')
        },
        layout: {
          key: 'layout',
          state: 'page',
          value: gettext('Application layout'),
          filterName: gettext('Layouts'),
          create: name => this.createPage(name, 'layout')
        },
        widget: {
          key: 'widget',
          state: 'widget',
          value: gettext('Custom widget'),
          filterName: gettext('Custom widgets'),
          create: name => this.createWidget(name),
          hasUniqueName: true
        }
      };
    }
    getFactory(type) {
      return this.factories[type];
    }
    getFactories() {
      return this.factories;
    }
    createPage(name, type) {
      return { type, name, rows: [[]] };
    }
    createWidget(name) {
      var template = '<!-- The custom widget template is defined here\n   - You can use standard HTML tags and AngularJS built-in directives, scope and interpolation system\n   - Custom widget properties defined on the right can be used as variables in a templates with properties.newProperty\n   - Functions exposed in the controller can be used with ctrl.newFunction()\n   - You can use the \'environment\' property injected in the scope when inside the Editor whiteboard. It allows to define a mockup\n     of the Custom Widget to be displayed in the whiteboard only. By default the widget is represented by an auto-generated icon\n     and its name (See the <span> below).\n-->\n \n<span ng-if="environment"><identicon name="{{environment.component.id}}" size="30" background-color="[255,255,255, 0]" foreground-color="[51,51,51]"></identicon> {{environment.component.name}}</span>\n\n<div style="color: {{ properties.color }}; background-color: {{ backgroudColor }}" ng-click="ctrl.toggleBackgroundColor()">\n    Value is:  <i>{{ properties.value }}</i>. Click me to toggle background color\n</div>';
      var controller = '/**\n * The controller is a JavaScript function that augments the AngularJS scope and exposes functions that can be used in the custom widget template\n * \n * Custom widget properties defined on the right can be used as variables in a controller with $scope.properties\n * To use AngularJS standard services, you must declare them in the main function arguments.\n * \n * You can leave the controller empty if you do not need it.\n */\nfunction ($scope) {\n    var white = \'white\';\n    \n    // add a new variable in AngularJS scope. It\'ll be usable in the template directly with {{ backgroudColor }} \n    $scope.backgroudColor = white;\n    \n    // define a function to be used in template with ctrl.toggleBackgroundColor()\n    this.toggleBackgroundColor = function() {\n        if ($scope.backgroudColor === white) {\n           // use the custom widget property backgroudColor with $scope.properties.backgroudColor\n            $scope.backgroudColor = $scope.properties.background;\n        } else {\n            $scope.backgroudColor = white;\n        }\n    };\n}';

      return {
        name,
        type: 'widget',
        template,
        controller,
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
            ]
          },
          {
            label: 'Background color on click',
            name: 'background',
            type: 'choice',
            defaultValue: 'Yellow',
            choiceValues: [
              'Yellow',
              'LightGray'
            ]
          }
        ]
      };
    }
  }
  angular
    .module('bonitasoft.designer.home')
    .factory('artifactFactories', (gettext) => new ArtifactFactories(gettext));

})();
