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
      var filters = assetPanel.filters.map((filter) => filter.getText());
      expect(filters).toContain('CSS');
      expect(filters).toContain('Image');
      expect(filters).toContain('JavaScript');

      assetPanel.filters.each((item) =>
        expect(item.element(by.tagName('input')).getAttribute('checked')).toBeTruthy()
      );
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
      assetPanel.filter('JavaScript').click();

      var types = assetPanel.types;
      expect(types).not.toContain('JavaScript');
      expect(types).toContain('CSS');
      expect(types).toContain('Image');
    });

    it('should filter assets which have a css type', function() {
      assetPanel.filter('CSS').click();

      var types = assetPanel.types;
      expect(types).not.toContain('CSS');
      expect(types).toContain('JavaScript');
      expect(types).toContain('Image');
    });

    it('should filter assets which have an image type', function() {
      assetPanel.filter('Image').click();

      var types = assetPanel.types;
      expect(types).not.toContain('Image');
      expect(types).toContain('JavaScript');
      expect(types).toContain('CSS');
    });

  });

  describe('for an asset', function() {

    describe('stored locally in a widget', function() {

      var widgetAsset;

      beforeEach(function() {
        widgetAsset = assetPanel.lines.first();
      });

      it('should display button to dowload/view the content', function() {
        let downloadButton = widgetAsset.element(by.css('button i.fa-alias-import'));
        expect(downloadButton.isPresent()).toBeTruthy();

        let viewButton = widgetAsset.element(by.css('button i.fa-search'));
        expect(viewButton.isPresent()).toBeTruthy();

        let editButton = widgetAsset.element(by.css('button i.fa-pencil'));
        expect(editButton.isPresent()).toBeFalsy();

        let deleteButton = widgetAsset.element(by.css('button i.fa-trash'));
        expect(deleteButton.isPresent()).toBeFalsy();

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

      it('should display button to dowload/view/delete the content', function() {

        let downloadButton = localAsset.element(by.css('button i.fa-alias-import'));
        expect(downloadButton.isPresent()).toBeTruthy();

        let viewButton = localAsset.element(by.css('button i.fa-search'));
        expect(viewButton.isPresent()).toBeTruthy();

        let editButton = localAsset.element(by.css('button i.fa-pencil'));
        expect(editButton.isPresent()).toBeFalsy();

        let deleteButton = localAsset.element(by.css('button i.fa-trash'));
        expect(deleteButton.isPresent()).toBeTruthy();
      });

      it('should display "Page level" like asset type and the name asset is "myStyle.css"', function() {
        var tds = localAsset.all(by.tagName('td'));

        expect(tds.get(2).getText()).toBe('Page level');
        expect(tds.get(1).getText()).toBe('myStyle.css');
      });

      it('should export an asset', function() {
        var btn = $$('.btn-bonita-asset').first();
        var iframe = $$('.ExportArtifact').first();
        btn.click();
        expect(iframe.getAttribute('src')).toMatch(/.*\/rest\/pages\/person\/assets\/css\/myStyle.css$/);
      });

    });

    describe('stored externally', function() {

      var localAsset;

      beforeEach(function() {
        localAsset = assetPanel.lines.get(1);
      });

      it('should display 2 buttons one to edit asset and another to remove it', function() {

        let downloadButton = localAsset.element(by.css('button i.fa-alias-import'));
        expect(downloadButton.isPresent()).toBeFalsy();

        let viewButton = localAsset.element(by.css('button i.fa-search'));
        expect(viewButton.isPresent()).toBeFalsy();

        let editButton = localAsset.element(by.css('button i.fa-pencil'));
        expect(editButton.isPresent()).toBeTruthy();

        let deleteButton = localAsset.element(by.css('button i.fa-trash'));
        expect(deleteButton.isPresent()).toBeTruthy();

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
