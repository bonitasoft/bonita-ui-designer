describe('appSelectorService', function() {

  'use strict';

  var appSelectorService, $localStorage;

  beforeEach(angular.mock.module('bonitasoft.designer.app-selector'));

  beforeEach(inject(function ($injector) {
    appSelectorService = $injector.get('appSelectorService');
    $localStorage = $injector.get('$localStorage');
  }));

  afterEach(function() {
    $localStorage.bonitaUIDesigner = undefined;
  });

  it('should contain the correct default path to appName', function(){
    expect(appSelectorService.getPathToLivingApp()).toBe('no-app-selected');
  });

  it('should update the pathToLivingApp', function () {
    appSelectorService.savePathToLivingApp('appPage1');

    expect(appSelectorService.getPathToLivingApp()).toBe('appPage1');
    expect($localStorage.bonitaUIDesigner.bosAppName).toBe('appPage1');
  });
});
