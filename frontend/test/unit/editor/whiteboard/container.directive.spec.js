(function() {
  'use strict';
  describe('container', function() {
    var $compile, $rootScope, element, directiveScope;

    beforeEach(function() {
      jasmine.addMatchers({
        toHaveMoveUpHandle: isPresent('.move-row-up'),
        toHaveMoveDownHandle: isPresent('.move-row-down'),
        toHaveDropRowBefore: checkDropRowVisibility('prev'),
        toHaveDropRowAfter: checkDropRowVisibility('next')
      });
    });

    beforeEach(angular.mock.module('bonitasoft.designer.editor.whiteboard'));

    beforeEach(inject(function(_$compile_, _$rootScope_) {
      $compile = _$compile_;
      $rootScope = _$rootScope_;

      // given an element containing the directive
      var template = '<container container="container" editor="editor" id="page"></container>';
      // when compiling with an input
      var row = [];
      var parentContainerRow = {
        row: row
      };
      $rootScope.container = {
        $$id: 'container-0',
        type: 'container',
        $$widget: {
          name: 'Container'
        },
        dimension: {
          xs: 12
        },
        rows: [
          []
        ],
        $$parentContainerRow: parentContainerRow
      };
      row.push($rootScope.container);

      $rootScope.editor = {
        isCurrentComponent: function() {
          return false;
        },
        isCurrentRow: function() {
          return false;
        }
      };

      element = $compile(template)($rootScope);
      directiveScope = element.isolateScope();
      $rootScope.$digest();
    }));

    // test for moving row buttons and visibility
    it('should not show a movable row when there is only one', function() {
      $rootScope.container.rows.splice(0, 1, [{}]);
      $rootScope.$apply();

      var row = element.find('.row-builder').first();

      expect(row).not.toHaveMoveUpHandle();
      expect(row).not.toHaveMoveDownHandle();
    });

    it('should only show move down handle on the first row', function() {
      $rootScope.container.rows.splice(0, 1, [{}], [{}]);
      $rootScope.$apply();

      var row = element.find('.row-builder').first();

      expect(row).not.toHaveMoveUpHandle();
      expect(row).toHaveMoveDownHandle();
    });

    it('should only show move up handle on the last row', function() {
      $rootScope.container.rows.splice(0, 1, [{}], [{}]);
      $rootScope.$apply();

      var row = element.find('.row-builder').last();

      expect(row).toHaveMoveUpHandle();
      expect(row).not.toHaveMoveDownHandle();
    });

    it('should show both move handles on a middle row', function() {
      $rootScope.container.rows.splice(0, 1, [{}], [{}], [{}]);
      $rootScope.$apply();

      var row = angular.element(element.find('.row-builder')[1]);

      expect(row).toHaveMoveUpHandle();
      expect(row).toHaveMoveDownHandle();
    });

    it('should show drop zones before and after the first row', function() {
      $rootScope.container.rows.splice(0, 1, [{}]);
      $rootScope.$apply();

      var row = element.find('.row-builder');

      expect(row).toHaveDropRowBefore();
      expect(row).toHaveDropRowAfter();
    });

    it('should show 1 drop zones all rows but the first', function() {
      $rootScope.container.rows.splice(0, 1, [{}], [{}]);
      $rootScope.$apply();

      var row = element.find('.row-builder').last();

      expect(row).not.toHaveDropRowBefore();
      expect(row).toHaveDropRowAfter();
    });

    it('should display a container in a container', function() {
      $rootScope.container.rows[0].push({
        $$id: 'container-1',
        type: 'container',
        $$templateUrl: 'js/editor/whiteboard/container-template.html',
        $$widget: {
          name: 'Container'
        },
        widgetId: 'pbContainer',
        dimension: {
          xs: 12
        },
        rows: [
          []
        ],
        $$parentContainerRow:  $rootScope.container.rows[0]
      });
      $rootScope.$apply();

      // and a container in the container
      expect(element.find('#container-1').length).toBe(1);
    });

    function createMatcher(matcher) {
      return function() {
        return {
          compare: function(actual, expected) {
            var result = {
              pass: matcher.match(actual, expected)
            };
            result.message = result.pass ? matcher.success : matcher.fail;
            return result;
          }
        };
      };
    }

    function isPresent(elementSelector) {
      var success = 'Expected element ' + elementSelector + ' to be present';
      return createMatcher({
        match: function(element) {
          return element.find(elementSelector).length >  0;
        },
        success: success,
        fail: success + ', but it wasn\'t'
      });
    }

    function checkDropRowVisibility(position) {
      var success = 'Expected drop row to be visible' + position === 'prev' ? 'before' : 'after';
      return createMatcher({
        match: function(element) {
          return element[position]() && angular.element(element[position]()[0]).hasClass('dropRow');
        },
        success: success,
        fail: success + ', but it wasn\'t'
      });
    }
  });
})();
