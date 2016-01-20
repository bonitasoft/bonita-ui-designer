/* jshint node: true */
var PageEditor = require('../pages/editor.page.js');
var path = require('path');

describe('asset panel', function() {
  var assetPanel, editor;

  beforeEach(function() {
    editor = PageEditor.get('person-page');
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
        expect(iframe.getAttribute('src')).toMatch(/.*\/rest\/pages\/person-page\/assets\/css\/myStyle.css$/);
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

  describe('pop-pup', () => {

    it('should add an external asset', function() {
      let popup = assetPanel.addAsset();
      expect(popup.title).toBe('Add a new asset');

      popup.type = 'JavaScript';
      popup.source = 'External';

      // invalid URL
      popup.url = 'notAnUrl ezr';
      expect($('input[name="url"] + div.text-danger').getText()).toBe(' Invalid URL: it must only contain alphanumeric character or #!:.?+=&%@-/');
      expect(popup.addBtn.isEnabled()).toBeFalsy();

      // already existing asset
      popup.url = 'https://github.myfile.js';
      expect($('input[name="url"] + div.text-danger').getText()).toBe(' A JavaScript asset named https://github.myfile.js is already added to assets.');
      expect(popup.addBtn.isEnabled()).toBeFalsy();

      popup.type = 'CSS';
      expect($('input[name="url"] + div.text-danger').isPresent()).toBeFalsy();
      popup.addBtn.click();

      expect(assetPanel.assets).toContain({ type: 'JavaScript', name: 'https://github.myfile.js'});
      expect(assetPanel.assets).toContain({ type: 'CSS', name: 'https://github.myfile.js'});
    });

    it('should edit an external asset', function() {
      let popup = assetPanel.editAsset('JavaScript', 'https://github.myfile.js');
      expect(popup.title).toBe('Edit asset https://github.myfile.js');
      expect(popup.type).toBe('JavaScript');
      expect($('select[name="source"]').isEnabled()).toBeFalsy();
      expect(popup.source).toBe('External');
      expect(popup.url).toBe('https://github.myfile.js');

      popup.url = 'https://new/file.js';
      popup.type = 'CSS';
      popup.saveBtn.click();

      expect(assetPanel.assets).toContain({ type: 'CSS', name: 'https://new/file.js'});
      expect(assetPanel.assets).not.toContain({ type: 'JavaScript', name: 'https://github.myfile.js'});
    });

    it('should add a local asset', function() {
      let popup = assetPanel.addAsset();
      popup.type = 'Image';
      popup.source = 'Local';

      popup.file = path.resolve(__dirname, 'protractor.png');
      expect($('div.file-upload + div.text-warning').getText()).toBe(' An Image asset named protractor.png is already added to assets.\nClick Add to override.');
      expect(popup.addBtn.isEnabled()).toBeTruthy();

      popup.file = path.resolve(__dirname, 'karma.png');
      expect($('div.file-upload + div.text-warning').isPresent()).toBeFalsy();

      // Cannot test add button since for now there is no way to mock http calls outside $http service.
    });
  });
});
