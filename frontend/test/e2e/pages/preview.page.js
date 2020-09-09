(function() {
  'use strict';

  var Preview = function() {
  };

  Preview.getPage = function(pageId) {
    browser.get('#/en/preview/page/' + (pageId || 'empty'));
    return new Preview();
  };

  Preview.getFragment = function(fragmentId) {
    browser.get('#/en/preview/fragment/' + (fragmentId || 'empty'));
    return new Preview();
  };

  module.exports = Preview;

  Preview.prototype = Object.create({}, {

    iframe: {
      get: function() {
        return element(by.tagName('iframe'));
      }
    },

    iframeWidth: {
      get: function() {
        return this.iframe.getAttribute('width');
      }
    },

    iframeSrc: {
      get: function() {
        return this.iframe.getAttribute('src');
      }
    }

  });

})();
