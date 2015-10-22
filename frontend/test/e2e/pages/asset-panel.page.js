(function () {
  'use strict';

  var AssetPanel = function () {
    this.sidebar = element(by.id('data-sidebar'));
  };

  module.exports = AssetPanel;

  var assetMethod = {

    open: function () {
      browser.actions().click(element.all(by.className('BottomPanel-toggle')).last()).perform();
    },

    element: function(selector) {
      return this.sidebar.element(selector);
    }

  };

  AssetPanel.prototype = Object.create(assetMethod, {

    addButton: {
      get: function () {
        return this.sidebar.element(by.buttonText('Add a new asset'));
      }
    },

    filters: {
      get: function(){
        return this.sidebar.all(by.repeater('(key, filter) in vm.filters'));
      }
    },

    lines: {
      get: function() {
        return this.sidebar.all(by.repeater('asset in vm.component.assets'));
      }
    },

    names: {
      get: function() {
        return this.lines.map(function (line) {
          return line.all(by.tagName('td')).get(1).getText();
        });
      }
    }

  });

})();
