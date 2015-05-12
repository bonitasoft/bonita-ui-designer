(function() {
  'use strict';

  var Preview = function() {
  };

  Preview.getPage = function(pageId) {
    browser.get('#/en/pages/' + (pageId || 'empty') + '/preview');
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
