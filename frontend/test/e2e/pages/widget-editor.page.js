(() => {
  'use strict';

  let EditLocalAssetPopUp = require('./edit-local-asset-popup.page');

  class AssetSection {

    constructor() {
      this.mainElement = element(by.css('div[ui-view="asset"]'));
    }

    list() {
      return this.mainElement.all(by.repeater('asset in vm.component.assets'));
    }

    editAsset(type, name) {
      this.list().filter(line => line.getText()
        .then(text => text.indexOf(type) > -1 && text.indexOf(name) > -1))
        .then(lines => lines[0].element(by.css('.fa-pencil')).click());

      return new EditLocalAssetPopUp();
    }
  }

  class WidgetEditor {
    static get(widgetName) {
      browser.get(`#/en/widget/${widgetName}`);

      //prevent onbeforeunload event to avoid blocking protractor when running tests
      //@see editor.page.js
      browser.executeScript('window.onbeforeunload = function(){};');

      return new WidgetEditor();
    }

    assets() {
      return new AssetSection();
    }
  }

  module.exports = WidgetEditor;
})();
