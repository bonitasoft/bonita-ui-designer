describe('pbButton', function () {

  var $compile, scope, element, $timeout, $parse, $q, $location, $window;

  beforeEach(module('org.bonitasoft.pagebuilder.widgets'));

  beforeEach(inject(function ($injector, $rootScope) {

    $q = $injector.get('$q');
    $compile = $injector.get('$compile');
    $httpBackend = $injector.get('$httpBackend');
    $timeout = $injector.get('$timeout');
    $parse = $injector.get('$parse');
    $location = $injector.get('$location');
    $window = $injector.get('$window');
    spyOn($window.top.location, 'assign');

    scope = $rootScope.$new();
    // set the default value for property method
    scope.properties = {
      method: "Submit task"
    };

    element = $compile('<pb-button></pb-button>')(scope);
    scope.$apply();
  }));


  it('should have specified label', function () {
    scope.properties.label = "foobar";
    scope.$apply();

    expect(element.find('button').text()).toBe("foobar");
  });

  it('should support changing text alignment', function () {
    scope.properties.alignment = "right";
    scope.$apply();

    element.find('button').triggerHandler('click');

    expect(element.find('div').hasClass("text-right")).toBeTruthy();
  });

  it('should be disablable', function () {
    scope.properties.disabled = true;
    scope.$apply();

    expect(element.find('button').attr('disabled')).toBe("disabled");
  });

  describe('remove from collection action', function () {

    beforeEach(function () {
      scope.properties.action = 'Remove from collection';
    });

    it('should remove last element from collection', function () {
      scope.properties.collectionToModify = ["apple", "banana"];
      scope.properties.collectionPosition = 'Last';
      scope.$apply();

      element.find('button').triggerHandler('click');

      expect(scope.properties.collectionToModify).toEqual(["apple"]);
    });

    it('should remove first element from collection', function () {
      scope.properties.collectionToModify = ["apple", "banana"];
      scope.properties.collectionPosition = 'First';
      scope.$apply();

      element.find('button').triggerHandler('click');

      expect(scope.properties.collectionToModify).toEqual(["banana"]);
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

    afterEach(function () {
      $httpBackend.verifyNoOutstandingRequest();
      $httpBackend.verifyNoOutstandingExpectation();
    });

    it('should POST some data to an API', function () {
      var data = {'name': 'toto'};
      var url = '/toto';

      $httpBackend.expectPOST(url, data).respond("yes");
      scope.properties.url = url;
      scope.properties.dataToSend = data;
      scope.properties.action = 'POST';

      scope.ctrl.action();
      $timeout.flush();
      $httpBackend.flush();

      expect(scope.properties.dataFromSuccess).toBe('yes');
    });

    it('should bind success data when POST succeed', function () {
      var data = {'name': 'toto'};
      var url = '/toto';

      $httpBackend.expectPOST(url, data).respond("success");
      scope.properties.url = url;
      scope.properties.dataToSend = data;
      scope.properties.action = 'POST';

      scope.ctrl.action();
      $timeout.flush();
      $httpBackend.flush();

      expect(scope.properties.dataFromSuccess).toBe('success');
    });

    it('should bind error data when POST fail', function () {
      var data = {'name': 'toto'};
      var url = '/toto'
      $httpBackend.expectPOST(url, data).respond(404, 'not found');

      scope.properties.dataToSend = data;
      scope.properties.action = 'POST';
      scope.properties.url = url;

      scope.ctrl.action();
      $timeout.flush();
      $httpBackend.flush();
      expect(scope.properties.dataFromError).toBe('not found');
    });

    it('should bind success data when GET succeed', function () {
      var data = {'name': 'toto'};
      var url = '/toto/1';

      $httpBackend.expectPUT(url, data).respond("success");
      scope.properties.dataToSend = data;
      scope.properties.action = 'PUT';
      scope.properties.url = url;

      scope.ctrl.action();
      $timeout.flush();
      $httpBackend.flush();

      expect(scope.properties.dataFromSuccess).toBe('success');
    });

    it('should change location on success', function () {
      scope.properties.action = 'PUT';
      scope.properties.url = '/some/location';
      scope.properties.targetUrlOnSuccess = '/new/location';
      $httpBackend.expectPUT('/some/location', scope.properties.dataToSend = {}).respond(200, '');

      scope.ctrl.action();

      $timeout.flush();
      $httpBackend.flush();
      expect($window.top.location.assign).toHaveBeenCalledWith('/new/location');
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
      expect($window.top.location.assign).not.toHaveBeenCalled();
    });

    it('should throw error when trying to remove from collection that is not an array', function () {
      scope.properties.action = 'Remove from collection';
      scope.properties.collectionToModify = {"an": "object"};
      scope.$apply();

      expect(scope.ctrl.action).toThrow('Collection property for widget button should be an array, but was [object Object]');
    });

    it('shoud throw error when trying to add to a collection that is not an array', function () {
      scope.properties.action = 'Add to collection';
      scope.properties.collectionToModify = {"an": "object"};
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
      $httpBackend.expectPOST('/bonita/API/bpm/userTask/42/execution', scope.properties.dataToSend).respond("success");

      element.find('button').triggerHandler('click');

      $timeout.flush();
      $httpBackend.flush();
    });

    it('should execute a userTask sending dataToSend and taking into account a specific user id', function () {
      $location.absUrl = function () {
        return 'http://localhost/bonita/portal/resource/taskInstance/ProcName/1.0/TaskName/content/?id=42&locale=en&user=1';
      };

      scope.properties.dataToSend = {'name': 'toto'};
      $httpBackend.expectPOST('/bonita/API/bpm/userTask/42/execution?user=1', scope.properties.dataToSend).respond("success");

      element.find('button').triggerHandler('click');

      $timeout.flush();
      $httpBackend.flush();
    });

    it('should not call the execute userTask api if the task instance id is missing in the URL', function () {
      $location.absUrl = function () {
        return 'http://localhost/bonita/portal/WrongURL';
      };

      element.find('button').triggerHandler('click');

      $timeout.flush();
      $httpBackend.verifyNoOutstandingRequest();
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
      $httpBackend.expectPOST('/bonita/API/bpm/process/8880000/instantiation', scope.properties.dataToSend).respond('success');

      element.find('button').triggerHandler('click');

      $timeout.flush();
      $httpBackend.flush();
    });

    it('should start process sending dataToSend and taking into account a specific user id', function () {
      $location.absUrl = function () {
        return 'http://localhost/bonita/portal/resource/process/ProcName/1.0/content/?locale=en&id=8880000&user=1';
      };

      scope.properties.dataToSend = {'name': 'toto'};
      $httpBackend.expectPOST('/bonita/API/bpm/process/8880000/instantiation?user=1', scope.properties.dataToSend).respond('success');

      element.find('button').triggerHandler('click');

      $timeout.flush();
      $httpBackend.flush();
    });

    it('should not call the start process api if the process definition id is missing in the URL', function () {
      $location.absUrl = function () {
        return 'http://localhost/bonita/portal/WrongURL';
      };

      element.find('button').triggerHandler('click');

      $timeout.flush();
      $httpBackend.verifyNoOutstandingRequest();
    });
  });
});
