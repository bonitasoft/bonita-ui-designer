var PageEditor = require('../pages/editor.page.js');

describe('custom widget test', function() {

  var editor;
  beforeEach(function() {
    editor = PageEditor.get('empty');
  });

  it('should allow adding a custom widget and edit it', function() {
    editor.addCustomWidget('customAwesomeWidget');

    // then we should be able to edit it
    $('#widgetActionButton').click();
    $('#editAction').click();

    // then we should go the custom widget edition
    expect($('.EditorHeader-brand').getText()).toBe('WIDGET EDITOR');
  });

});
