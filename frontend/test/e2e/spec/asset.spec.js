var PageEditor = require('../pages/editor.page.js');

describe('asset panel', function() {
  var assetPanel, editor;

  beforeEach(function() {
    editor = PageEditor.get('person');
    assetPanel = editor.assetPanel();
    //The asset panel is not opened by default
    assetPanel.open();
  });

  describe('on init', function() {

    it('should display a button "Add a new asset"', function() {
      expect(assetPanel.addButton.getText()).toBe('Add a new asset');
    });

    it('should display 3 checked checkboxes to filter the asset list', function() {
      var filters = assetPanel.filters;
      expect(filters.getText()).toEqual([ 'CSS', 'Image', 'JavaScript' ]);
      Array.from(filters).forEach((item) => expect(item.element(by.tagName('input')).getAttribute('checked')).toBeTruthy());
    });

    it('should display a help button', function() {
      assetPanel.element(by.className('btn-asset--help')).click();
      expect($('.modal-header .modal-title').getText()).toBe('Help');
    });

  });


  describe('on filtering', function() {

    it('should display a table with 4 assets', function() {
      expect(assetPanel.lines.count()).toBe(4);
    });

    it('should filter assets which have a js type', function() {
      var filters = assetPanel.filters;
      filters.last().element(by.tagName('input')).click();

      expect(assetPanel.lines.first().all(by.tagName('td')).get(3).getText()).toBe('CSS');
      expect(assetPanel.lines.get(1).all(by.tagName('td')).get(3).getText()).toBe('Image');
      expect(assetPanel.lines.last().all(by.tagName('td')).get(3).getText()).toBe('CSS');
    });

    it('should filter assets which have a css type', function() {
      var filters = assetPanel.filters;
      filters.first().element(by.tagName('input')).click();

      expect(assetPanel.lines.first().all(by.tagName('td')).get(3).getText()).toBe('JavaScript');
      expect(assetPanel.lines.last().all(by.tagName('td')).get(3).getText()).toBe('Image');
    });

  });


  describe('for an asset', function(){

    describe('stored locally in a widget', function() {

      var widgetAsset;

      beforeEach(function() {
        widgetAsset = assetPanel.lines.first();
      });

      it('should display 1 button to view the content', function() {
        var buttons = widgetAsset.all(by.className('btn'));

        expect(buttons.count()).toBe(1);
        expect(buttons.first().getAttribute('title')).toBe('View asset content');
      });

      it('should display the widget name in the table', function() {
        expect(widgetAsset.all(by.tagName('td')).get(2).getText()).toBe('customWidget');
      });

    });

    describe('stored locally', function() {

      var localAsset;

      beforeEach(function() {
        localAsset = assetPanel.lines.last();
      });

      it('should display 2 buttons one to delete an asset and another to view asset', function() {
        var buttons = localAsset.all(by.className('btn'));

        expect(buttons.count()).toBe(2);
        expect(buttons.first().getAttribute('title')).toBe('View asset content');
        expect(buttons.last().getAttribute('title')).toBe('Delete asset');
      });


      it('should display "Page level" like asset type and the name asset is "myStyle.css"', function() {
        var tds = localAsset.all(by.tagName('td'));

        expect(tds.get(2).getText()).toBe('Page level');
        expect(tds.get(1).getText()).toBe('myStyle.css');
      });

    });

    describe('stored externally', function() {

      var localAsset;

      beforeEach(function() {
        localAsset = assetPanel.lines.get(1);
      });

      it('should display 2 buttons one to edit asset and another to remove it', function() {
        var buttons = localAsset.all(by.className('btn'));

        expect(buttons.count()).toBe(2);
        expect(buttons.first().getAttribute('title')).toBe('Update asset');
        expect(buttons.last().getAttribute('title')).toBe('Delete asset');
      });


      it('should has a name with a prefix http', function() {
        var tds = localAsset.all(by.tagName('td'));

        tds.get(1).getText().then(function(name) {
          expect(name.indexOf('http')).toBe(0);
        });
      });

    });
  });

  it('should be updated while adding/removing widgets with assets', function() {
    // adding two custom widget with assets
    editor.addCustomWidget('customAwesomeWidget');
    editor.addCustomWidget('customAwesomeWidget');

    // asset panel should list  customAwesomeWidget's assets
    expect(assetPanel.names).toContain('awesome-gif.gif', 'https://awesome.cdn.com/cool.js');

    // removing one customAwesomeWidget, asset panel should still list customAwesomeWidget's assets
    editor.removeWidget();
    expect(assetPanel.names).toContain('awesome-gif.gif', 'https://awesome.cdn.com/cool.js');

    // removing last customAwesomeWidget, asset panel should not list customAwesomeWidget's assets anymore
    editor.removeWidget();
    expect(assetPanel.names).not.toContain('awesome-gif.gif', 'https://awesome.cdn.com/cool.js');
  });
});
