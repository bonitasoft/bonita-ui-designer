/**
 * Wrap ace directive according to page builder needs
 */
angular.module('pb.directives').directive('aceEditor', function(aceDataCompleter) {

  'use strict';

  var langTools = ace.require('ace/ext/language_tools');

  return {
    restrict: 'E',
    replace: true,
    scope: {
      mode: '@',
      autoCompletion: '@'
    },
    template: '<div ui-ace="{ mode: \'{{mode}}\', showGutter: true, onLoad: loaded }"></div>',
    controller: function($scope, $parse, $attrs ) {
      var ctrl = this;

      $scope.loaded = function(editor) {
        if (!editor) {
          return;
        }
        if ($attrs.autoCompletion) {
          var dataCompleter = aceDataCompleter($scope.$eval($scope.autoCompletion));
          langTools.setCompleters([dataCompleter, langTools.keyWordCompleter ]);
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
          bindKey: {win: 'Ctrl-Alt-Shift-D', mac: 'Ctrl-Alt-Shift-D'},
          exec: function(editor) {
            editor.setValue('');
          },
          readOnly: false
        });

      };

      $scope.$on('$destroy', function(){
        if (ctrl.editor.completer) {
          ctrl.editor.completer.detach();
        }
        ctrl.editor.destroy();
      });
    }
  };
});
