describe('CustomWidgetEditorCtrl', function () {

  var $window, browserHistoryService;

  beforeEach(angular.mock.module('bonitasoft.designer.common.services'));

  beforeEach(inject(function ($rootScope, _$window_, _browserHistoryService_) {
    $window = _$window_;
    browserHistoryService = _browserHistoryService_;
  }));

  it('should navigate to home page', function () {
    let fallback = jasmine.createSpy();
    browserHistoryService.back(fallback);
    expect(fallback).toHaveBeenCalled();
  });

  it('should navigate back', function () {
    let newState = {
      foo: 'bar'
    };
    $window.history.pushState(newState, 'new state entry', 'fake-page.html');
    let fallback = jasmine.createSpy();
    browserHistoryService.back(fallback);
    expect(fallback).not.toHaveBeenCalled();
  });
});
