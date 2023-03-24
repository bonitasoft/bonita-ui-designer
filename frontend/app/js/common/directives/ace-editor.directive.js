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
 * Wrap ace directive according to page builder needs
 */
angular.module('bonitasoft.designer.common.directives').directive('aceEditor', function(aceDataCompleter) {

  'use strict';

  var langTools = ace.require('ace/ext/language_tools');

  return {
    restrict: 'E',
    replace: true,
    scope: {
      mode: '@',
      autoCompletion: '@'
    },
    template: '<div ui-ace="{ mode: \'{{mode}}\', showGutter: true, onLoad: loaded, useWrapMode: true }"></div>',
    controller: function($scope, $parse, $attrs) {
      var ctrl = this;

      $scope.loaded = function(editor) {
        if (!editor) {
          return;
        }
        editor.$blockScrolling = Infinity;
        if ($attrs.autoCompletion) {
          var dataCompleter = aceDataCompleter($scope.$eval($scope.autoCompletion));
          langTools.setCompleters([dataCompleter, langTools.keyWordCompleter]);
          editor.setOptions({
            enableBasicAutocompletion: true,
            enableLiveAutocompletion: true
          });
        }

        ctrl.editor = editor;

        editor.setShowPrintMargin(false);

        // used by e2e tests to clear the editor
        editor.commands.addCommand({
          name: 'deleteAll',
          bindKey: { win: 'Ctrl-Alt-Shift-D', mac: 'Ctrl-Alt-Shift-D' },
          exec: function(editor) {
            editor.setValue('');
          },
          readOnly: false
        });

        // unbind ctrl-alt-e key since it prevent typing the euros sign on windows
        // see https://github.com/ajaxorg/ace/wiki/Default-Keyboard-Shortcuts#other
        // relates to https://bonitasoft.atlassian.net/browse/BS-16364
        editor.commands.bindKey('Ctrl-Alt-E', null);
      };
    }
  };
});
