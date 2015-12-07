describe('widgetRepo', function() {
  var widgetRepo, $httpBackend;

  beforeEach(angular.mock.module('bonitasoft.designer.common.repositories'));
  beforeEach(inject(function(_$httpBackend_, _widgetRepo_) {
    widgetRepo = _widgetRepo_;
    $httpBackend = _$httpBackend_;
  }));

  it('should save a widget', function() {
    var widget = {
      id: 'awesome-custom-widget',
      name: 'Awesome Widget',
      template: '<div>Hello {{ properties.value }}</div>',
      properties: [
        { name: 'value', label: 'Value', type: 'text' }
      ]
    };
    $httpBackend.expectPUT('rest/widgets/awesome-custom-widget', widget).respond(200);

    widgetRepo.save(widget);

    $httpBackend.flush();
  });

  it('should get all widgets', function() {
    var expectedWidgets = [{ id: 'label', name: 'Label' }, { id: 'input', name: 'Input' }];
    $httpBackend.expectGET('rest/widgets').respond(expectedWidgets);

    widgetRepo.all().then(function(widgets) {
      expect(widgets).toEqual(expectedWidgets);
    });

    $httpBackend.flush();
  });

  it('should get widget by ID', function() {
    var widget;
    $httpBackend.expectGET('rest/widgets/label').respond({ id: 'label', name: 'Label' });

    widgetRepo.load('label').then(function(response) {
      widget = response.data;
    });

    $httpBackend.flush();
    expect(widget.name).toBe('Label');
  });

  it('should create a widget with default template and controller', function() {
    var widget = {
      name: 'newWidgetName',
      template: '<div ng-click="ctrl.sayClicked()">Enter your template here, using {{ properties.value }}</div>',
      controller: 'function($scope) {\n    this.sayClicked = function() {\n        $scope.properties.value = \'clicked\';\n    };\n}',
      custom: true,
      properties: [
        {
          label: 'Value',
          name: 'value',
          type: 'text',
          defaultValue: 'This is the initial value'
        }
      ]
    };
    $httpBackend.expectPOST('rest/widgets', widget).respond(200);

    widgetRepo.create(widget);

    $httpBackend.flush();
  });

  it('should duplicate a widget', function() {
    var widget = {
      name: 'foo'
    };

    var createdWidget = {
      id: 'generated-id',
      name: 'foo'
    };

    $httpBackend.expectPOST('rest/widgets?duplicata=src-widget-id').respond(201, createdWidget);

    var result;
    widgetRepo.create(widget, 'src-widget-id').then(function(data) {
      result = data;
    });

    $httpBackend.flush();
    expect(result).toEqual(createdWidget);
  });

  it('should delete a widget', function() {
    $httpBackend.expectDELETE('rest/widgets/awesome-custom-widget').respond(200);

    widgetRepo.delete('awesome-custom-widget');

    $httpBackend.flush();
  });

  it('should get all custom widgets', function() {
    $httpBackend.expectGET('rest/widgets?view=light').respond([
      { id: 'label', custom: false },
      { id: 'custom', custom: true }
    ]);

    var customs = [];
    widgetRepo.customs()
      .then(function(widgets) {
        customs = widgets;
      });

    $httpBackend.flush();
    expect(customs.length).toBe(1);
  });

  it('should add a property to a widget', function() {
    var property = { name: 'value', label: 'Value', type: 'text' };
    $httpBackend.expectPOST('rest/widgets/awesome-custom-widget/properties', property).respond(200);

    widgetRepo.addProperty('awesome-custom-widget', property);

    $httpBackend.flush();
  });

  it('should update a property of a widget', function() {
    var property = { name: 'value', label: 'Value', type: 'text' };
    $httpBackend.expectPUT('rest/widgets/awesome-custom-widget/properties/toBeUpdated', property).respond(200);

    widgetRepo.updateProperty('awesome-custom-widget', 'toBeUpdated', property);

    $httpBackend.flush();
  });

  it('should delete a property of a widget', function() {
    $httpBackend.expectDELETE('rest/widgets/awesome-custom-widget/properties/toBeDeleted').respond(200);

    widgetRepo.deleteProperty('awesome-custom-widget', 'toBeDeleted');

    $httpBackend.flush();
  });

  it('should compute widget export url', function() {
    var widget = { id: 'widgetId' };

    var url = widgetRepo.exportUrl(widget);

    expect(url).toBe('export/widget/widgetId');
  });

  it('should delete a local asset', function() {
    var asset = {
      id: 'UIID',
      name: 'myfile.js',
      type: 'js'
    };
    $httpBackend.expectDELETE('rest/widgets/my-widget/assets/UIID').respond(200, []);

    widgetRepo.deleteAsset('my-widget', asset);
    $httpBackend.flush();
  });

  it('should delete an external asset', function() {
    var asset = {
      id: 'UIID',
      name: 'http://mycdn.com/myfile.js',
      type: 'js'
    };
    $httpBackend.expectDELETE('rest/widgets/my-widget/assets/UIID').respond(200, []);

    widgetRepo.deleteAsset('my-widget', asset);
    $httpBackend.flush();
  });

  it('should save an asset', function() {
    var asset = {
      name: 'http://mycdn.com/myfile.js',
      type: 'js'
    };
    $httpBackend.expectPOST('rest/widgets/my-widget/assets').respond(200);
    widgetRepo.createAsset('my-widget', asset);
    $httpBackend.flush();
  });

  it('should decrement order of an asset', function() {
    var asset = {
      id: 'UIID',
      name: 'http://mycdn.com/myfile.js',
      type: 'js'
    };
    $httpBackend.expectPUT('rest/widgets/my-widget/assets/UIID?decrement=true').respond(200);
    widgetRepo.decrementOrderAsset('my-widget', asset);
    $httpBackend.flush();
  });

  it('should increment order of an asset', function() {
    var asset = {
      id: 'UIID',
      name: 'http://mycdn.com/myfile.js',
      type: 'js'
    };
    $httpBackend.expectPUT('rest/widgets/my-widget/assets/UIID?increment=true').respond(200);
    widgetRepo.incrementOrderAsset('my-widget', asset);
    $httpBackend.flush();
  });

  it('should mark a widget as favorite', function() {
    $httpBackend.expectPUT('rest/widgets/widget-id/favorite', true).respond('');

    widgetRepo.markAsFavorite('widget-id');

    $httpBackend.flush();
  });

  it('should unmark a widget as favorite', function() {
    $httpBackend.expectPUT('rest/widgets/widget-id/favorite', false).respond('');

    widgetRepo.unmarkAsFavorite('widget-id');

    $httpBackend.flush();
  });
});
