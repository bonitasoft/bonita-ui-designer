var PageEditor = require('../pages/editor.page.js');

describe('moving rows test', function() {

  var editor;
  beforeEach(function() {
    editor = PageEditor.get('empty');

    editor.addElement('pbParagraph').to('.dropRow--last');
    editor.addElement('pbInput').to('.dropRow--last');
  });

  it('should not allow moving first row up', function() {
    expect(editor.rows.first().element(by.css('.move-row-up')).isPresent()).toBe(false);
  });

  it('should not allow moving last row down', function() {
    expect(editor.rows.last().element(by.css('.move-row-down')).isPresent()).toBe(false);
  });

  it('should allow moving a row up', function() {
    // simulate a mouse over on the row
    browser.actions().mouseMove( editor.rows.get(1), {x:1, y:1} ).perform();
    editor.rows.get(1).element(by.css('.move-row-up')).click();
    expect(editor.rows.first().all(by.css('component p')).count()).toBe(1);
  });

  it('should allow moving a row down', function() {
    // simulate a mouse over on the row
    browser.actions().mouseMove( editor.rows.get(1), {x:1, y:1} ).perform();
    editor.rows.get(1).element(by.css('.move-row-down')).click();
    expect(editor.rows.last().all(by.css('component p')).count()).toBe(1);
  });
});
