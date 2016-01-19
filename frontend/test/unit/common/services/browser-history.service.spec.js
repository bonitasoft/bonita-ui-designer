describe('CustomWidgetEditorCtrl', function() {

  var $window, browserHistoryService;

  beforeEach(angular.mock.module('bonitasoft.designer.common.services'));

  beforeEach(inject(function($rootScope, _$window_, _browserHistoryService_) {
    $window = _$window_;
    browserHistoryService = _browserHistoryService_;
    $window.history =  {
      back: jasmine.createSpy(),
    };
  }));

  it('should navigate to home page', function() {
    $window.history.length = 1;
    let fallback = jasmine.createSpy();
    browserHistoryService.back(fallback);
    expect($window.history.back).not.toHaveBeenCalled();
    expect(fallback).toHaveBeenCalled();
  });
  it('should navigate back', function() {
    $window.history.length = 4;
    browserHistoryService.back();
    expect($window.history.back).toHaveBeenCalled();
  });
});
