(function() {

  'use strict';

  angular
    .module('bonitasoft.designer.editor')
    .run(function(whiteboardService, editorService) {

      whiteboardService.registerOnWidgetAddFunction(editorService.addWidgetAssetsToPage);
      whiteboardService.registerOnWidgetRemoveFunction(editorService.removeAssetsFromPage);

    });

})();
