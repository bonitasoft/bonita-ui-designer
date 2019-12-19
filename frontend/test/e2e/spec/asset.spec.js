/* jshint node: true */
var PageEditor = require('../pages/editor.page.js');
var path = require('path');

describe('asset panel', function() {
  var assetPanel, editor;
  let NAME_COLUMN = 1;
  let SCOPE_COLUMN = 3;

  beforeEach(function () {
    editor = PageEditor.get('person-page');
    assetPanel = editor.assetPanel();
    //The asset panel is not opened by default
    assetPanel.open();
  });

  describe('on init', function () {
    it('should display a button "Add a new asset"', function () {
      expect(assetPanel.addButton.getText()).toBe('Add a new asset');
    });

    it('should display 3 checked checkboxes to filter the asset list', function () {
      var filters = assetPanel.filters.map((filter) => filter.getText());
      expect(filters).toContain('Page');
      expect(filters).toContain('Widget');
      expect(filters).toContain('Base Framework');


      it('should display page checkbox checked by default', function () {
        let pageAssetFilter = assetPanel.filters.get(0);

        expect(pageAssetFilter.element(by.tagName('input')).getAttribute('checked')).toBeTruthy();
      });

      it('should display page checkbox checked by default', function () {
        let widgetAssetFilter = assetPanel.filters.get(1);

        expect(widgetAssetFilter.element(by.tagName('input')).getAttribute('checked')).toBeFalsy();
      });
      it('should display a help button', function () {
        assetPanel.element(by.className('btn-asset--help')).click();
        expect($('.modal-header .modal-title').getText()).toBe('Help');
      });
    });

    describe('With widget and page filters checked', function () {
      beforeEach(function () {
        assetPanel.filters.get(1).click();
      });
      describe('on filtering', function () {
        it('should display a table with 4 assets', function () {
          expect(assetPanel.lines.count()).toBe(4);
        });
      });

      describe('for an asset', function () {
        it('should display the widget name in the table for a widget asset', function () {
          let widgetAsset = assetPanel.lines.get(1);
          expect(widgetAsset.all(by.tagName('td')).get(SCOPE_COLUMN).getText()).toBe('customWidget');
        });

        describe('stored locally', function () {

          var localAsset;

          beforeEach(function () {
            localAsset = assetPanel.lines.first();
          });

          it('should display action buttons for assets', function () {
            // Editable page asset should have downnload/edit/delete
            let editableAsset = assetPanel.getAssetByName('myStyle.css');
            expect(editableAsset.actions.download.isPresent()).toBeTruthy();
            expect(editableAsset.actions.view.isPresent()).toBeFalsy();
            expect(editableAsset.actions.edit.isPresent()).toBeTruthy();
            expect(editableAsset.actions.delete.isPresent()).toBeTruthy();

            // Non editable page asset should have downnload/view/delete
            let nonEditableAsset = assetPanel.getAssetByName('protractor.png');
            expect(nonEditableAsset.actions.download.isPresent()).toBeTruthy();
            expect(nonEditableAsset.actions.view.isPresent()).toBeTruthy();
            expect(nonEditableAsset.actions.edit.isPresent()).toBeFalsy();
            expect(nonEditableAsset.actions.delete.isPresent()).toBeTruthy();

            editor.addCustomWidget('customAwesomeWidget');
            // External widget asset should have no action
            let externalWidgetAsset = assetPanel.getAssetByName('https://awesome.cdn.com/cool.js');
            expect(externalWidgetAsset.actions.delete.isPresent()).toBeFalsy();
            expect(externalWidgetAsset.actions.view.isPresent()).toBeFalsy();
            expect(externalWidgetAsset.actions.download.isPresent()).toBeFalsy();
            expect(externalWidgetAsset.actions.edit.isPresent()).toBeFalsy();

            // Local widget asset should have download/view
            let internalWidgetAsset = assetPanel.getAssetByName('myWidgetStyle.css');
            expect(internalWidgetAsset.actions.delete.isPresent()).toBeFalsy();
            expect(internalWidgetAsset.actions.view.isPresent()).toBeTruthy();
            expect(internalWidgetAsset.actions.download.isPresent()).toBeTruthy();
            expect(internalWidgetAsset.actions.edit.isPresent()).toBeFalsy();
          });

          it('should display "Page level" like asset type and the name asset is "myStyle.css"', function () {
            var tds = localAsset.all(by.tagName('td'));

            expect(tds.get(SCOPE_COLUMN).getText()).toBe('Page level');
            expect(tds.get(NAME_COLUMN).getText()).toBe('myStyle.css');
          });

          it('should export an asset', function () {
            let internalCSS = assetPanel.getAssetByName('myStyle.css');
            var iframe = $$('.ExportArtifact').first();
            internalCSS.actions.download.click();
            expect(iframe.getAttribute('src')).toMatch(/.*\/rest\/pages\/person-page\/assets\/css\/myStyle.css$/);
          });

        });

        describe('stored externally', function () {

          var localAsset;

          beforeEach(function () {
            localAsset = assetPanel.lines.get(2);
          });

          it('should display 2 buttons one to edit asset and another to remove it', function () {

            let downloadButton = localAsset.element(by.css('button i.fa-alias-import'));
            expect(downloadButton.isPresent()).toBeFalsy();

            let viewButton = localAsset.element(by.css('button i.fa-search'));
            expect(viewButton.isPresent()).toBeFalsy();

            let editButton = localAsset.element(by.css('button i.fa-pencil'));
            expect(editButton.isPresent()).toBeTruthy();

            let deleteButton = localAsset.element(by.css('button i.fa-trash'));
            expect(deleteButton.isPresent()).toBeTruthy();

          });

          it('should has a name with a prefix http', function () {
            var tds = localAsset.all(by.tagName('td'));

            tds.get(NAME_COLUMN).getText().then(function (name) {
              expect(name.indexOf('http')).toBe(0);
            });
          });

        });
      });

      it('should be updated while adding/removing widgets with assets', function () {
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

        it('should add an external asset', function () {
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

          expect(assetPanel.assets).toContain({name: 'https://github.myfile.js', source: 'External'});
        });

        it('should edit an external asset', function () {
          let popup = assetPanel.editAsset('https://github.myfile.js');
          expect(popup.title).toBe('Edit asset https://github.myfile.js');
          expect(popup.type).toBe('JavaScript');
          expect($('select[name="source"]').isEnabled()).toBeFalsy();
          expect(popup.source).toBe('External');
          expect(popup.url).toBe('https://github.myfile.js');

          popup.url = 'https://new/file.js';
          popup.type = 'CSS';
          popup.saveBtn.click();

          expect(assetPanel.assets).toContain({source: 'External', name: 'https://new/file.js'});
        });


        it('should add a local asset', function () {
          let popup = assetPanel.addAsset();
          popup.type = 'Image';
          popup.source = 'Local';

          popup.file = path.resolve(__dirname, 'resources', 'protractor.png');
          expect($('div.file-upload + div.text-warning').getText()).toBe(' An Image asset named protractor.png is already added to assets.\nClick Add to override.');
          expect(popup.addBtn.isEnabled()).toBeTruthy();

          popup.file = path.resolve(__dirname, 'resources', 'karma.png');
          expect($('div.file-upload + div.text-warning').isPresent()).toBeFalsy();

          // Cannot test add button since for now there is no way to mock http calls outside $http service.
        });
      });

      it('should allow to edit a local asset', () => {
        // should display asset file content
        let popup = assetPanel.editAsset('myStyle.css');
        let someRule = '.somecssrule {\n  color: blue\n}';
        expect(popup.fileContent).toBe(someRule);

        // should update content
        expect(popup.dismissBtn.getText()).toEqual('Close');
        let someNewRule = '.somenewrule {color: red}';
        // Warning: set fileContent add content (instead of overriding it) when the editor has the focus
        popup.fileContent = someNewRule;
        // Note: for some reasons, a ';' is added by selenium when a css rule is added
        let expectedContent = someNewRule.concat(';', someRule);
        expect(popup.fileContent).toBe(expectedContent);
        expect(popup.dismissBtn.getText()).toEqual('Discard changes');
        popup.save();
        expect(popup.isOpen()).toBeTruthy();
        popup.dismissBtn.click();
        assetPanel.editAsset('myStyle.css');
        expect(popup.fileContent).toBe(expectedContent);

        // should not update file content while clicking on cancel
        popup.fileContent = 'Again some fresh content';
        popup.dismissBtn.click();
        expect(popup.isOpen()).toBeFalsy();
        assetPanel.editAsset('myStyle.css');
        expect(popup.fileContent).toBe(expectedContent);
      });
    });
  });

});
