describe('pbButton', function () {

  var $compile, scope, element, $timeout, $parse, $q, $location, $window, $httpBackend, localStorageService;

  beforeEach(module('bonitasoft.ui.services', 'bonitasoft.ui.widgets'));

  beforeEach(inject(function ($injector, $rootScope) {

    $q = $injector.get('$q');
    $compile = $injector.get('$compile');
    $httpBackend = $injector.get('$httpBackend');
    $timeout = $injector.get('$timeout');
    $parse = $injector.get('$parse');
    $location = $injector.get('$location');
    $window = $injector.get('$window');
    localStorageService = $injector.get('localStorageService');
    spyOn($window.location, 'assign');
    spyOn($window.parent, 'postMessage');

    scope = $rootScope.$new();
    // set the default value for property method
    scope.properties = {
      method: 'Submit task',
      allowHTML: true
    };

    element = $compile('<pb-button></pb-button>')(scope);
    scope.$apply();
  }));


  it('should have specified label', function () {
    scope.properties.label = '<i class="fa fa-bonita">foobar</i>';
    scope.$apply();

    expect(element.find('button').text()).toBe('foobar');
    expect(element.find('button').html()).toBe('<i class="fa fa-bonita">foobar</i>');
  });

  it('should support changing text alignment', function () {
    scope.properties.alignment = 'right';
    scope.$apply();

    element.find('button').triggerHandler('click');

    expect(element.find('div').hasClass('text-right')).toBeTruthy();
  });

  it('should be disablable', function () {
    scope.properties.disabled = true;
    scope.$apply();

    expect(element.find('button').attr('disabled')).toBe('disabled');
  });

  describe('remove from collection action', function () {

    beforeEach(function () {
      scope.properties.action = 'Remove from collection';
    });

    it('should remove last element from collection', function () {
      scope.properties.collectionToModify = ['apple', 'banana'];
      scope.properties.collectionPosition = 'Last';
      scope.$apply();

      element.find('button').triggerHandler('click');

      expect(scope.properties.collectionToModify).toEqual(['apple']);
    });

    it('should remove first element from collection', function () {
      scope.properties.collectionToModify = ['apple', 'banana'];
      scope.properties.collectionPosition = 'First';
      scope.$apply();

      element.find('button').triggerHandler('click');

      expect(scope.properties.collectionToModify).toEqual(['banana']);
    });

    it('should do nothing when removing last of an empty collection', function () {
      scope.properties.collectionToModify = [];
      scope.properties.collectionPosition = 'Last';
      scope.$apply();

      element.find('button').triggerHandler('click');

      expect(scope.properties.collectionToModify).toEqual([]);
    });

    it('shoud do nothing when removing first of an empty collection', function () {
      scope.properties.collectionToModify = [];
      scope.properties.collectionPosition = 'First';
      scope.$apply();

      element.find('button').triggerHandler('click');

      expect(scope.properties.collectionToModify).toEqual([]);
    });

    it('should do nothing when removing from undefined', function () {
      scope.properties.collectionToModify = undefined;
      scope.properties.collectionPosition = 'First';
      scope.$apply();

      element.find('button').triggerHandler('click');

      expect(scope.properties.collectionToModify).toEqual(undefined);
    });

    it('should remove $item element from collection when current item is selected', function () {
      scope.properties.collectionToModify = ['apple', 'banana'];
      scope.properties.collectionPosition = 'Item';
      scope.properties.removeItem = 'apple';
      scope.$apply();

      element.find('button').triggerHandler('click');

      expect(scope.properties.collectionToModify).toEqual(['banana']);
    });

    it('should do nothing when removing unknow item', function () {
      scope.properties.collectionToModify = ['apple', 'banana'];
      scope.properties.collectionPosition = 'Item';
      scope.properties.removeItem = 'lemon';
      scope.$apply();

      element.find('button').triggerHandler('click');

      expect(scope.properties.collectionToModify).toEqual(['apple', 'banana']);
    });
  });

  describe('add to collection action', function () {
    beforeEach(function () {
      scope.properties.action = 'Add to collection';
    });

    it('should add last element to a collection', function () {
      scope.properties.collectionToModify = ['apple', 'banana'];
      scope.properties.collectionPosition = 'Last';
      scope.$apply();

      element.find('button').triggerHandler('click');

      expect(scope.properties.collectionToModify).toEqual(['apple', 'banana', undefined]);
    });

    it('should add first element to a collection', function () {
      scope.properties.collectionToModify = ['apple', 'banana'];
      scope.properties.collectionPosition = 'First';
      scope.$apply();

      element.find('button').triggerHandler('click');

      expect(scope.properties.collectionToModify).toEqual([undefined, 'apple', 'banana']);
    });

    it('should init array if collection is undefined', function () {
      scope.$apply();

      element.find('button').triggerHandler('click');

      expect(scope.properties.collectionToModify).toEqual([undefined]);
    });

    it('should add a copy of a user defined value if not empty', function () {
      scope.properties.collectionToModify = [{name: 'apple'}, {name: 'banana'}];
      scope.properties.collectionPosition = 'Last';
      scope.properties.valueToAdd = {name: 'kiwi'};
      scope.$apply();

      element.find('button').triggerHandler('click');

      expect(scope.properties.collectionToModify.indexOf(scope.properties.valueToAdd)).toBe(-1);
      expect(scope.properties.collectionToModify).toContain(scope.properties.valueToAdd);
    });
  });

  describe('controller actions', function () {

    var originalFrameElement;

    beforeEach(function () {
      originalFrameElement = $window.frameElement;
      $window.frameElement = Object.create($window.frameElement);
    });

    afterEach(function () {
      $httpBackend.verifyNoOutstandingRequest();
      $httpBackend.verifyNoOutstandingExpectation();
      $window.frameElement = originalFrameElement;
    });

    it('should POST some data to an API', function () {
      var data = {'name': 'toto'};
      var url = '/toto';

      $httpBackend.expectPOST(url, data).respond('yes');
      scope.properties.url = url;
      scope.properties.dataToSend = data;
      scope.properties.action = 'POST';

      scope.ctrl.action();
      $timeout.flush();
      $httpBackend.flush();

      expect(scope.properties.dataFromSuccess).toBe('yes');
      expect(scope.properties.dataFromError).toBe(undefined);
    });

    it('should bind success data when POST succeed', function () {
      var data = {'name': 'toto'};
      var url = '/toto';

      $httpBackend.expectPOST(url, data).respond('success');
      scope.properties.url = url;
      scope.properties.dataToSend = data;
      scope.properties.action = 'POST';

      scope.ctrl.action();
      $timeout.flush();
      $httpBackend.flush();

      expect(scope.properties.dataFromSuccess).toBe('success');
      expect(scope.properties.dataFromError).toBe(undefined);
    });

    it('should bind error data when POST fail', function () {
      var data = {'name': 'toto'};
      var url = '/toto';
      $httpBackend.expectPOST(url, data).respond(404, 'not found');

      scope.properties.dataToSend = data;
      scope.properties.action = 'POST';
      scope.properties.url = url;

      scope.ctrl.action();
      $timeout.flush();
      $httpBackend.flush();
      expect(scope.properties.dataFromError).toBe('not found');
      expect(scope.properties.dataFromSuccess).toBe(undefined);
    });

    it('should bind response status code when POST succeeds', function() {
      var data = {'name': 'toto'};
      var url = '/toto';
      $httpBackend.expectPOST(url, data).respond(200, 'success');

      scope.properties.dataToSend = data;
      scope.properties.action = 'POST';
      scope.properties.url = url;

      scope.ctrl.action();
      $timeout.flush();
      $httpBackend.flush();
      expect(scope.properties.responseStatusCode).toBe(200);
    });

    it('should bind response status code when POST fail', function() {
      var data = {'name': 'toto'};
      var url = '/toto';
      $httpBackend.expectPOST(url, data).respond(500, 'error');

      scope.properties.dataToSend = data;
      scope.properties.action = 'POST';
      scope.properties.url = url;

      scope.ctrl.action();
      $timeout.flush();
      $httpBackend.flush();
      expect(scope.properties.responseStatusCode).toBe(500);
    });

    it('should bind success data when GET succeed', function () {
      var url = '/toto';

      $httpBackend.expectGET(url).respond('success');
      scope.properties.url = url;
      scope.properties.action = 'GET';

      scope.ctrl.action();
      $timeout.flush();
      $httpBackend.flush();

      expect(scope.properties.dataFromSuccess).toBe('success');
      expect(scope.properties.dataFromError).toBe(undefined);
    });

    it('should bind error data when GET fail', function () {
      var url = '/toto';
      $httpBackend.expectGET(url).respond(404, 'not found');

      scope.properties.action = 'GET';
      scope.properties.url = url;

      scope.ctrl.action();
      $timeout.flush();
      $httpBackend.flush();
      expect(scope.properties.dataFromError).toBe('not found');
      expect(scope.properties.dataFromSuccess).toBe(undefined);
    });

    it('should bind response status code when GET succeeds', function() {
      var url = '/toto';
      $httpBackend.expectGET(url).respond(200, 'success');

      scope.properties.action = 'GET';
      scope.properties.url = url;

      scope.ctrl.action();
      $timeout.flush();
      $httpBackend.flush();
      expect(scope.properties.responseStatusCode).toBe(200);
    });

    it('should bind response status code when GET fail', function() {
      var url = '/toto';
      $httpBackend.expectGET(url).respond(500, 'error');

      scope.properties.action = 'GET';
      scope.properties.url = url;

      scope.ctrl.action();
      $timeout.flush();
      $httpBackend.flush();
      expect(scope.properties.responseStatusCode).toBe(500);
    });

    it('should not change location on GET success', function () {
      scope.properties.action = 'GET';
      scope.properties.url = '/some/location';
      scope.properties.targetUrlOnSuccess = '/new/location';
      $httpBackend.expectGET('/some/location').respond(200, '');

      scope.ctrl.action();

      $timeout.flush();
      $httpBackend.flush();
      expect($window.location.assign).not.toHaveBeenCalledWith('/new/location');
    });

    it('should bind success data when PUT succeed', function () {
      var data = {'name': 'toto'};
      var url = '/toto/1';

      $httpBackend.expectPUT(url, data).respond('success');
      scope.properties.dataToSend = data;
      scope.properties.action = 'PUT';
      scope.properties.url = url;

      scope.ctrl.action();
      $timeout.flush();
      $httpBackend.flush();

      expect(scope.properties.dataFromSuccess).toBe('success');
      expect(scope.properties.dataFromError).toBe(undefined);
    });

    it('should bind response status code data when PUT succeed', function () {
      var data = {'name': 'toto'};
      var url = '/toto/1';

      $httpBackend.expectPUT(url, data).respond(200, 'success');
      scope.properties.dataToSend = data;
      scope.properties.action = 'PUT';
      scope.properties.url = url;

      scope.ctrl.action();
      $timeout.flush();
      $httpBackend.flush();

      expect(scope.properties.responseStatusCode).toBe(200);
    });

    it('should bind response status code data when PUT fail', function () {
      var data = {'name': 'toto'};
      var url = '/toto/1';

      $httpBackend.expectPUT(url, data).respond(500, 'error');
      scope.properties.dataToSend = data;
      scope.properties.action = 'PUT';
      scope.properties.url = url;

      scope.ctrl.action();
      $timeout.flush();
      $httpBackend.flush();

      expect(scope.properties.responseStatusCode).toBe(500);
    });

    it('should change location on success', function () {
      $window.frameElement = null;
      scope.properties.action = 'PUT';
      scope.properties.url = '/some/location';
      scope.properties.targetUrlOnSuccess = '/new/location';
      $httpBackend.expectPUT('/some/location', scope.properties.dataToSend = {}).respond(200, '');

      scope.ctrl.action();

      $timeout.flush();
      $httpBackend.flush();
      expect($window.location.assign).toHaveBeenCalledWith('/new/location');
    });

    it('should change location on success when in iframe', function () {
      $window.frameElement.id = 'someframe';
      scope.properties.action = 'PUT';
      scope.properties.url = '/some/location';
      scope.properties.targetUrlOnSuccess = '/new/location';
      $httpBackend.expectPUT('/some/location', scope.properties.dataToSend = {}).respond(200, '');

      scope.ctrl.action();

      $timeout.flush();
      $httpBackend.flush();
      expect($window.location.assign).toHaveBeenCalledWith('/new/location');
    });

    it('should not change location on success when in bonitaframe', function () {
      $window.frameElement.id = 'bonitaframe';
      scope.properties.action = 'PUT';
      scope.properties.url = '/some/location';
      scope.properties.targetUrlOnSuccess = '/new/location';
      $httpBackend.expectPUT('/some/location', scope.properties.dataToSend = {}).respond(200, '');

      scope.ctrl.action();

      $timeout.flush();
      $httpBackend.flush();
      expect($window.location.assign).not.toHaveBeenCalled();
    });

    it('should bind error data when PUT fail', function () {
      var data = {'name': 'toto'};
      var url = '/toto/1';

      $httpBackend.expectPUT(url, data).respond(404, 'not found');
      scope.properties.dataToSend = data;
      scope.properties.action = 'PUT';
      scope.properties.url = url;

      scope.ctrl.action();
      $timeout.flush();
      $httpBackend.flush();
      expect(scope.properties.dataFromError).toBe('not found');
      expect(scope.properties.dataFromSuccess).toBe(undefined);
      expect($window.location.assign).not.toHaveBeenCalled();
    });

    it('should bind success data when DELETE succeed', function () {
      var url = '/toto';

      $httpBackend.expectDELETE(url).respond('success');
      scope.properties.url = url;
      scope.properties.action = 'DELETE';

      scope.ctrl.action();
      $timeout.flush();
      $httpBackend.flush();

      expect(scope.properties.dataFromSuccess).toBe('success');
      expect(scope.properties.dataFromError).toBe(undefined);
    });

    it('should bind error data when DELETE fail', function () {
      var url = '/toto';
      $httpBackend.expectDELETE(url).respond(404, 'not found');

      scope.properties.action = 'DELETE';
      scope.properties.url = url;

      scope.ctrl.action();
      $timeout.flush();
      $httpBackend.flush();
      expect(scope.properties.dataFromError).toBe('not found');
      expect(scope.properties.dataFromSuccess).toBe(undefined);
    });

    it('should bind response status code data when DELETE fail', function () {
      var url = '/toto';
      $httpBackend.expectDELETE(url).respond(500, 'error');

      scope.properties.action = 'DELETE';
      scope.properties.url = url;

      scope.ctrl.action();
      $timeout.flush();
      $httpBackend.flush();
      expect(scope.properties.responseStatusCode).toBe(500);
    });

    it('should bind response status code data when DELETE fail', function () {
      var url = '/toto';
      $httpBackend.expectDELETE(url).respond(200, 'success');

      scope.properties.action = 'DELETE';
      scope.properties.url = url;

      scope.ctrl.action();
      $timeout.flush();
      $httpBackend.flush();
      expect(scope.properties.responseStatusCode).toBe(200);
    });

    it('should throw error when trying to remove from collection that is not an array', function () {
      scope.properties.action = 'Remove from collection';
      scope.properties.collectionToModify = {'an': 'object'};
      scope.$apply();

      expect(scope.ctrl.action).toThrow('Collection property for widget button should be an array, but was [object Object]');
    });

    it('shoud throw error when trying to add to a collection that is not an array', function () {
      scope.properties.action = 'Add to collection';
      scope.properties.collectionToModify = {'an': 'object'};
      scope.$apply();

      expect(scope.ctrl.action).toThrow('Collection property for widget button should be an array, but was [object Object]');
    });
  });

  describe('Submit task action', function () {

    beforeEach(function () {
      scope.properties.action = 'Submit task';
    });

    afterEach(function () {
      $httpBackend.verifyNoOutstandingRequest();
      $httpBackend.verifyNoOutstandingExpectation();
    });

    it('should execute a userTask sending dataToSend', function () {
      $location.absUrl = function () {
        return 'http://localhost/bonita/portal/resource/taskInstance/ProcName/1.0/TaskName/content/?id=42&locale=en';
      };

      scope.properties.dataToSend = {'name': 'toto'};
      $httpBackend.expectPOST('../API/bpm/userTask/42/execution', scope.properties.dataToSend).respond('success');

      scope.ctrl.action();

      $timeout.flush();
      $httpBackend.flush();
    });

	it('should assign and execute a userTask sending dataToSend', function () {
      $location.absUrl = function () {
        return 'http://localhost/bonita/portal/resource/taskInstance/ProcName/1.0/TaskName/content/?id=42&locale=en';
      };

      scope.properties.dataToSend = {'name': 'toto'};
	  scope.properties.assign = true;
      $httpBackend.expectPOST('../API/bpm/userTask/42/execution?assign=true', scope.properties.dataToSend).respond('success');

    scope.ctrl.action();

      $timeout.flush();
      $httpBackend.flush();
    });


    it('should execute a userTask sending dataToSend and taking into account a specific user id', function () {
      $location.absUrl = function () {
        return 'http://localhost/bonita/portal/resource/taskInstance/ProcName/1.0/TaskName/content/?id=42&locale=en&user=1';
      };

      scope.properties.dataToSend = {'name': 'toto'};
      $httpBackend.expectPOST('../API/bpm/userTask/42/execution?user=1', scope.properties.dataToSend).respond('success');

      scope.ctrl.action();

      $timeout.flush();
      $httpBackend.flush();
    });

    it('should not call the execute userTask api if the task instance id is missing in the URL', function () {
      $location.absUrl = function () {
        return 'http://localhost/bonita/portal/WrongURL';
      };

      scope.ctrl.action();

      $httpBackend.verifyNoOutstandingRequest();
    });

    it('should remove LocalStorage entry for the current task URL', function () {
      $location.absUrl = function () {
        return 'http://localhost/bonita/portal/resource/taskInstance/ProcName/1.0/TaskName/content/?id=42&locale=en&user=1';
      };

      // Given some values stored in the LocalStorage
      var savedData = "to be deleted";
      var url = $window.location.href;
      localStorageService.save(url, savedData);

      scope.properties.dataToSend = {'name': 'toto'};
      $httpBackend.expectPOST('../API/bpm/userTask/42/execution?user=1', scope.properties.dataToSend).respond('success');

      scope.ctrl.action();

      $timeout.flush();
      $httpBackend.flush();

      expect(localStorageService.read(url)).toEqual(null);
    });
  });

  describe('Start process action', function () {

    beforeEach(function () {
      scope.properties.action = 'Start process';
    });

    afterEach(function () {
      $httpBackend.verifyNoOutstandingRequest();
      $httpBackend.verifyNoOutstandingExpectation();
    });

    it('should start process sending dataToSend', function () {
      $location.absUrl = function () {
        return 'http://localhost/bonita/portal/resource/process/ProcName/1.0/content/?locale=en&id=8880000';
      };

      scope.properties.dataToSend = {'name': 'toto'};
      $httpBackend.expectPOST('../API/bpm/process/8880000/instantiation', scope.properties.dataToSend).respond('success');

      scope.ctrl.action();

      $timeout.flush();
      $httpBackend.flush();
    });

    it('should start process sending dataToSend and taking into account a specific user id', function () {
      $location.absUrl = function () {
        return 'http://localhost/bonita/portal/resource/process/ProcName/1.0/content/?locale=en&id=8880000&user=1';
      };

      scope.properties.dataToSend = {'name': 'toto'};
      $httpBackend.expectPOST('../API/bpm/process/8880000/instantiation?user=1', scope.properties.dataToSend).respond('success');

      scope.ctrl.action();

      $timeout.flush();
      $httpBackend.flush();
    });

    it('should not call the start process api if the process definition id is missing in the URL', function () {
      $location.absUrl = function () {
        return 'http://localhost/bonita/portal/WrongURL';
      };

      scope.ctrl.action();

      $httpBackend.verifyNoOutstandingRequest();
    });

    it('should remove LocalStorage entry for the current process URL', function () {
      $location.absUrl = function () {
        return 'http://localhost/bonita/portal/resource/process/ProcName/1.0/content/?locale=en&id=8880000&user=1';
      };

      // Given some values stored in the LocalStorage
      var savedData = "to be deleted";
      var url = $window.location.href;
      localStorageService.save(url, savedData);

      scope.properties.dataToSend = {'name': 'toto'};
      scope.properties.dataToSend = {'name': 'toto'};
      $httpBackend.expectPOST('../API/bpm/process/8880000/instantiation?user=1', scope.properties.dataToSend).respond('success');

      scope.ctrl.action();

      $timeout.flush();
      $httpBackend.flush();

      expect(localStorageService.read(url)).toEqual(null);
    });

  });

  describe('postMessage', function () {

    var currentWindow;

    var stringifiedJSONArgsMatcher = function(actualArgs, expectedArgs) {
      for (var i= 0; i < actualArgs.length; i++) {
        var argBeforeStringify;
        try {
          argBeforeStringify = JSON.parse(actualArgs[i]);
        } catch (e) {
          argBeforeStringify = actualArgs[i];
        }
        if (!jasmine.matchersUtil.equals(argBeforeStringify, expectedArgs[i])) {
          return false;
        }
      }
      return true;
    };

    beforeEach(function () {
      currentWindow = $window.self;
    });

    afterEach(function () {
      $window.self = currentWindow;
      $httpBackend.verifyNoOutstandingRequest();
      $httpBackend.verifyNoOutstandingExpectation();

    });

    it('should be sent on success', function () {
      jasmine.addCustomEqualityTester(stringifiedJSONArgsMatcher);
      $window.self = null;
      var responseStatus = 200;
      scope.properties.action = 'PUT';
      scope.properties.url = '/some/location';
      scope.properties.targetUrlOnSuccess = '/new/location';

      let caseResponse = {case: 2};
      $httpBackend.expectPUT('/some/location', scope.properties.dataToSend = {}).respond(responseStatus, caseResponse);

      scope.ctrl.action();

      $timeout.flush();
      $httpBackend.flush();

      expect($window.parent.postMessage).toHaveBeenCalledWith(jasmine.objectContaining(
        {
          message: 'success',
          status: responseStatus,
          dataFromSuccess: caseResponse,
          targetUrlOnSuccess: scope.properties.targetUrlOnSuccess
        }), '*');
    });

    it('should be sent on error', function () {
      jasmine.addCustomEqualityTester(stringifiedJSONArgsMatcher);
      $window.self = null;
      var responseStatus = 500;
      scope.properties.action = 'PUT';
      scope.properties.url = '/some/location';
      scope.properties.targetUrlOnSuccess = '/new/location';
      $httpBackend.expectPUT('/some/location', scope.properties.dataToSend = {}).respond(responseStatus, 'fileTooBig');

      scope.ctrl.action();

      $timeout.flush();
      $httpBackend.flush();

      expect($window.parent.postMessage).toHaveBeenCalledWith(jasmine.objectContaining(
        {
          message: 'error',
          status: responseStatus,
          dataFromError: 'fileTooBig',
          targetUrlOnSuccess: scope.properties.targetUrlOnSuccess
        }), '*');
    });

    it('should not be sent if not in an iframe', function () {
      $window.self = $window.parent;
      scope.properties.action = 'PUT';
      scope.properties.url = '/some/location';
      $httpBackend.expectPUT('/some/location', scope.properties.dataToSend = {}).respond(200, {caseId:1});

      scope.ctrl.action();

      $timeout.flush();
      $httpBackend.flush();
      expect($window.parent.postMessage).not.toHaveBeenCalled();
    });

    it('should disable the button during request', function () {
      scope.properties.action = 'PUT';
      scope.properties.url = '/some/location';
      $httpBackend.expectPUT('/some/location', scope.properties.dataToSend = {}).respond(200);

      element.find('button').click();
      scope.$apply();

      expect(element.find('button').attr('disabled')).toBeTruthy();
      $httpBackend.flush();
      expect(element.find('button').attr('disabled')).toBeFalsy();
    });
  });
});
