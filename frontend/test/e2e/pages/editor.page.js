/* global browser, by, element */

var DataPanel = require('./data-panel.page');
var AssetPanel = require('./asset-panel.page');

(function() {
  'use strict';

  function PageEditor() {
  }

  PageEditor.get = function(pageId) {
    browser.get('#/en/pages/' + pageId || 'empty');

    // wait for angular to be ready;
    browser.waitForAngular();

    //remove onbeforeunload to avoid confirm popup at the end of a test
    //so protractor can continue to run tests
    browser.executeScript('window.onbeforeunload = function(){};');
    return new PageEditor();
  };

  module.exports = PageEditor;

  /**
   * Drag an item to a destination
   * @param  {String} element CSS selector
   * @return {Object}
   */
  function drag(element) {
    var simulateDragDrop = '$(\'%source%\').simulateDragDrop({ dropTarget: \'%dest%\'})';

    return {
      /**
       * Drop destination for a node
       * @param  {String} dropZone CSS selector
       * @param {Boolean} first Select the first item
       * @param {Boolean} last Select the last item
       * @return {void}
       */
      andDropOn: function(dropZone, first, last) {

        var suffix = '';

        if (first) {
          suffix = ':first';
        }
        if (last) {
          suffix = ':last';
        }

        var command = simulateDragDrop
          .replace('%source%', element)
          .replace('%dest%', dropZone + suffix);
        browser.executeScript(command);
      }
    };
  }

  var pageEditor = {

    // set selected widget/container width
    setWidth: function(width) {
      var input = element(by.id('width'));
      input.clear();
      input.sendKeys(width);
    },

    // add a widget by its id
    addElement: function(elementId) {
      var draggedElement = drag('#' + elementId);
      return {
        to: draggedElement.andDropOn
      };
    },

    drag: function(selector) {
      return drag(selector);
    },

    // add a widget by its id
    addWidget: function(widgetId) {
      var editor = this;
      var btn = $('.Palette-section[aria-label=widgets]');
      btn.getAttribute('class').then(function(className) {
        if (!/.Palette-section--active/.test(className)) {
          btn.click();
        }
        editor.addElement(widgetId).to('.widget-placeholder');
      });
    },
    addCustomWidget: function(widgetId) {
      var editor = this;
      var btn = $('.Palette-section[aria-label="custom widgets"]');
      btn.getAttribute('class').then(function(className) {
        if (!/.Palette-section--active/.test(className)) {
          btn.click();
        }
        expect($('identicon[name="' + widgetId + '"] img').getAttribute('src')).toContain('data:image/png;base64,');
        editor.addElement(widgetId).to('.widget-placeholder');
      });
    },
    // remove selected widget
    removeWidget: function() {
      var selectedItem = element(by.css('.component-element--selected'));
      browser.actions().mouseMove(selectedItem).perform();
      $$('.component-element--selected .fa.fa-times-circle').first().click();
    },

    // add a container
    addContainer: function() {
      this.addElement('container').to('.widget-placeholder', false, true);
    },

    // add a Tabs container
    addTabsContainer: function() {
      this.addElement('tabsContainer').to('.widget-placeholder', false, true);
    },

    // add a Tabs container
    addFormContainer: function() {
      this.addElement('formContainer').to('.widget-placeholder', false, true);
    },

    dataPanel: function() {
      return new DataPanel();
    },

    assetPanel: function() {
      return new AssetPanel();
    },

    property: function(propertyName) {
      return $('#widget-properties').element(by.id('property-' + propertyName));
    },

    back: function() {
      $('.EditorHeader-icon').click();
    }
  };

  PageEditor.prototype = Object.create(pageEditor, {

    // Palette listing widgets
    palette: {
      get: function() {
        return element(by.css('.Palette'));
      }
    },

    // editor rows
    rows: {
      get: function() {
        return element.all(by.css('#editor > .widget-wrapper > [ng-repeat="row in container.rows"]'));
      }
    },

    properties: {
      get: function() {
        return $('#widget-properties');
      }
    },

    // editor components (i.e component wrap a widget)
    components: {
      get: function() {
        return element.all(by.tagName('component'));
      }
    },

    // get all containers
    containers: {
      get: function() {
        return element.all(by.tagName('container'));
      }
    },
    // get all containers
    tabsContainers: {
      get: function() {
        return element.all(by.tagName('tabs-container'));
      }
    },
    // get all formContainers
    formContainers: {
      get: function() {
        return element.all(by.tagName('form-container'));
      }
    },
    // get all containers
    containersInEditor: {
      get: function() {
        return element.all(by.css('container:not(#editor)'));
      }
    },

    // get the menu bar
    menu: {
      get: function() {
        return element(by.css('.EditorHeader'));
      }
    }
  });
})();
