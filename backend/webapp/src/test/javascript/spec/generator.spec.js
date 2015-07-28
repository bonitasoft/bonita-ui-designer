(function () {
  'use strict';
  describe('bonitasoft.ui module and its dependencies to former module', function() {
    beforeEach(function() {
      //these modules are not loaded in karma conf
      angular.module('ngMessages', []);
      angular.module('ngUpload', []);
      angular.module('bonitasoft.ui.templates', []);
    });
    beforeEach(module('bonitasoft.ui'));
    beforeEach(function() {
      angular.module('pb.generator').filter('someTimeAgo', function () {
        return function someTimeAgo() {
          return 'some time ago...';
        };
      });
      angular.module('pb.widgets').directive('pbTestWidget', function(){
        return {
          restrict: 'AE',
          template: '<div id="testWidget">{{ "Bonitasoft" | someTimeAgo }}</div>'
        };
      });
    });
    it('should instanciate the directive', inject(function($compile, $rootScope) {
      var element = $compile('<pb-test-widget></pb-test-widget')($rootScope);
      $rootScope.$apply();
      expect(element.find('#testWidget').text()).toEqual('some time ago...');

    }));
  });
})();
