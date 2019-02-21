(function() {
  'use strict';

  describe('assetScope filter', function() {
    var $filter, assets, scopes;

    beforeEach(angular.mock.module('bonitasoft.designer.assets'));

    beforeEach(inject(function(_$filter_) {
      $filter = _$filter_;

      assets = [
        { 'name': 'MyAbcExample.js', 'type': 'js', 'scope':'page'},
        { 'name': 'MyAbcExample.css', 'type': 'css', 'scope':'widget' },
        { 'name': 'MyAbcExample.png', 'type': 'img', 'scope':'page' }
      ];

      scopes = {
        page: { label: 'Page', filter: true },
        widget: { label: 'Widget', filter: true }
      };

    }));

    it('should not filter when no arg scope', function() {
      expect($filter('assetFilter')(assets)).toEqual(assets);
    });

    it('should not filter when filtering items are true', function() {
      expect($filter('assetFilter')(assets, scopes)).toEqual(assets);
    });

    it('should exclude asset who attach on page', function() {
      scopes.page.filter = false;

      expect($filter('assetFilter')(assets, scopes)).toEqual([
        { 'name': 'MyAbcExample.css', 'type': 'css', 'scope':'widget' }
      ]);
    });

    it('should exclude asset who attach on widget', function() {
      scopes.widget.filter = false;

      expect($filter('assetFilter')(assets, scopes)).toEqual([
        { 'name': 'MyAbcExample.js', 'type': 'js', 'scope':'page'},
        { 'name': 'MyAbcExample.png', 'type': 'img', 'scope':'page' }
      ]);
    });
  });

})();
