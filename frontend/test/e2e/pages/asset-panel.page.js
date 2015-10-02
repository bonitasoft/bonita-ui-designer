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
    },

    addAsset: function (id, type, name) {

      this.addButton.click();

      var form = element(by.name('addAsset'));

      form.element(by.model('newAsset.type')).element(by.cssContainingText('option', type)).click();
      if(name.indexOf('http')===0){
        form.element(by.model('newAsset.source')).element(by.cssContainingText('option', 'External')).click();
        form.element(by.model('newAsset.name')).sendKeys(name);
      }
      else{
        form.element(by.model('newAsset.source')).element(by.cssContainingText('option', 'Local')).click();
        form.element(by.model('newAsset.name')).sendKeys(name);
      }

      $('.modal-footer').element(by.buttonText('Add')).click();

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
