(function() {
  'use strict';

  class AssetPopUp {
    get title() {
      return $('.modal-content .modal-title').getText();
    }

    get addBtn() {
      return element(by.cssContainingText('.modal-footer button', 'Add'));
    }

    get saveBtn() {
      return element(by.cssContainingText('.modal-footer button', 'Save'));
    }

    set url(value) {
      $('.modal-content input[name="url"]').clear().sendKeys(value);
    }

    get url() {
      return $('input[name="url"]').getAttribute('value');
    }

    set file(path) {
      $('.modal-content input[type="file"]').clear().sendKeys(path);
    }

    get file() {
      return $('.modal-content input[type="file"]').getAttribute('value');
    }

    set type(type) {
      element(by.cssContainingText('select[name="type"] option', type)).click();
    }

    get type() {
      return element(by.css('select[name="type"]')).$('option:checked').getText();
    }

    set source(source) {
      element(by.cssContainingText('select[name="source"] option', source)).click();
    }

    get source() {
      return element(by.css('select[name="source"]')).$('option:checked').getText();
    }
  }


  var AssetPanel = function() {
    this.sidebar = element(by.id('data-sidebar'));
  };

  module.exports = AssetPanel;

  var assetMethod = {

    open: function() {
      browser.actions().click(element.all(by.className('BottomPanel-toggle')).last()).perform();
    },

    element: function(selector) {
      return this.sidebar.element(selector);
    },

    filter: function(name) {
      return this.sidebar.element(by.cssContainingText('.search-filter label', name));
    },

    addAsset: function() {
      this.addButton.click();
      return new AssetPopUp();
    },

    editAsset: function(type, name) {
      this.lines.filter(line => line.getText().then(text => text.indexOf(type) > -1 && text.indexOf(name) > -1))
        .then(lines => lines[0].element(by.css('.fa-pencil')).click());
      return new AssetPopUp();
    }
  };

  AssetPanel.prototype = Object.create(assetMethod, {

    addButton: {
      get: function() {
        return this.sidebar.element(by.buttonText('Add a new asset'));
      }
    },

    filters: {
      get: function() {
        return this.sidebar.all(by.repeater('(key, filter) in vm.filters'));
      }
    },

    lines: {
      get: function() {
        return this.sidebar.all(by.repeater('asset in vm.component.assets'));
      }
    },

    // asset names in asset list
    names: {
      get: function() {
        return this.lines.map(function(line) {
          return line.all(by.tagName('td')).get(1).getText();
        });
      }
    },

    // asset types in asset list
    types: {
      get: function() {
        return this.lines.map(function(line) {
          return line.all(by.tagName('td')).get(3).getText();
        });
      }
    },

    assets: {
      get: function() {
        return this.lines.map(function(line) {
          let tds = line.all(by.tagName('td'));
          return {
            name: tds.get(1).getText(),
            type: tds.get(3).getText()
          };
        });
      }
    }

  });

})();
