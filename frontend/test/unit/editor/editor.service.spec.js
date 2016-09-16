(function() {
  'use strict';

  describe('editor service', function() {
    var $rootScope, $httpBackend, $q, widgetRepo, pageRepo, editorService, alerts, components, whiteboardComponentWrapper, whiteboardService;

    var labelWidget = {
      id: 'label',
      custom: false,
      type: 'component',
      properties: {
        text: 'string',
        alignment: 'string'
      }
    };

    var json = {
      'data': {
        'id': 'person',
        'name': 'person page',
        'rows': [
          [
            {
              'type': 'container',
              'dimension': { 'xs': 12 },
              'rows': [
                [
                  {
                    'type': 'component',
                    'id': 'label',
                    'dimension': { 'xs': 12 },
                    'propertyValues': { 'text': 'label 1', 'alignment': 'left' }
                  }
                ],
                [
                  {
                    'type': 'component',
                    'id': 'label',
                    'dimension': { 'xs': 12 },
                    'propertyValues': { 'text': 'label 2', 'alignment': 'left' }
                  }
                ],
                [
                  {
                    'type': 'tabsContainer',
                    'dimension': { 'xs': 12 },
                    'tabs': [
                      {
                        'title': 'Tab 1',
                        'container': {
                          'type': 'container',
                          'rows': [
                            []
                          ]
                        }
                      }
                    ]
                  }
                ]
              ]
            }
          ], [
            {
              'type': 'formContainer',
              'dimension': {
                'xs': 12
              },
              'propertyValues': {
                'cssClasses': {
                  'type': 'constant',
                  'value': ''
                },
                'isDisplayed': {
                  'type': 'constant',
                  'value': true
                },
                'url': {
                  'type': 'constant',
                  'value': ''
                },
                'method': {
                  'type': 'constant',
                  'value': ''
                },
                'action': {
                  'type': 'constant',
                  'value': null
                }
              },
              'container': {
                'type': 'container',
                'dimension': {
                  'xs': 12
                },
                'propertyValues': {
                  'cssClasses': {
                    'type': 'constant',
                    'value': ''
                  },
                  'isDisplayed': {
                    'type': 'constant',
                    'value': true
                  }
                },
                'rows': []
              },
              'name': '',
              'method': '',
              'action': null
            }
          ]
        ]
      }
    };

    beforeEach(angular.mock.module('bonitasoft.designer.editor'));

    beforeEach(inject(function($injector) {
      $rootScope = $injector.get('$rootScope');
      $httpBackend = $injector.get('$httpBackend');
      $q = $injector.get('$q');

      widgetRepo = $injector.get('widgetRepo');
      pageRepo = $injector.get('pageRepo');

      editorService = $injector.get('editorService');
      components = $injector.get('components');
      whiteboardComponentWrapper = $injector.get('whiteboardComponentWrapper');
      alerts = $injector.get('alerts');
      whiteboardService = $injector.get('whiteboardService');

      spyOn(widgetRepo, 'all').and.returnValue($q.when([labelWidget]));
      spyOn(alerts, 'addError');
    }));

    it('should initialize a page', function() {
      var page = {};
      spyOn(pageRepo, 'load').and.returnValue($q.when(json));
      editorService.initialize(pageRepo, 'person')
        .then(function(data) {
          page = data;
        });

      $rootScope.$apply();

      expect(page.rows[0][0].$$id).toBe('container-0');
      expect(page.rows[0][0].$$widget.name).toBe('Container');
      expect(page.rows[0][0].rows[0][0].$$id).toBe('component-0');
      expect(page.rows[0][0].rows[0][0].$$widget).toEqual(labelWidget);
      expect(page.rows[0][0].rows[0][0].$$parentContainerRow.container).toBe(page.rows[0][0]);
      expect(page.rows[0][0].rows[0][0].$$parentContainerRow.row).toBe(page.rows[0][0].rows[0]);
      expect(page.rows[0][0].rows[1][0].$$parentContainerRow.container).toBe(page.rows[0][0]);
      expect(page.rows[0][0].rows[1][0].$$parentContainerRow.row).toBe(page.rows[0][0].rows[1]);

      var tabsContainer = page.rows[0][0].rows[2][0];
      expect(tabsContainer.$$parentContainerRow.container).toBe(page.rows[0][0]);
      expect(tabsContainer.$$parentContainerRow.row).toBe(page.rows[0][0].rows[2]);
      expect(tabsContainer.$$widget.name).toBe('Tabs container');
      expect(tabsContainer.tabs[0].$$parentTabsContainer).toBe(tabsContainer);

      var formContainer = page.rows[1][0];
      expect(formContainer.$$id).toBe('formContainer-0');
      expect(formContainer.$$widget.name).toBe('Form container');
      expect(formContainer.$$parentContainerRow.container).toBe(page);
    });

    it('should add palette', function() {
      var spy = jasmine.createSpy('bar');
      spyOn(pageRepo, 'load').and.returnValue($q.when(json));

      editorService.addPalette('foo', spy);
      editorService.initialize(pageRepo, 'person');
      $rootScope.$apply();

      expect(spy).toHaveBeenCalled();
    });

    it('should init components', function() {

      spyOn(components, 'register');
      spyOn(components, 'reset');
      spyOn(whiteboardComponentWrapper, 'wrapPage').and.returnValue({});
      spyOn(pageRepo, 'load').and.returnValue($q.when(json));

      editorService.initialize(pageRepo, 'person');
      $rootScope.$apply();

      expect(components.reset).toHaveBeenCalled();
      expect(components.reset.calls.count()).toBe(1);

      expect(components.register).toHaveBeenCalled();
      expect(components.register.calls.count()).toBe(3);
    });

    it('should add an alert if initialize failed', function() {
      spyOn(pageRepo, 'load').and.returnValue($q.reject('load failed'));

      editorService.initialize(pageRepo, 'person');
      $rootScope.$apply();

      expect(alerts.addError).toHaveBeenCalled();
    });

    it('should remove widget assets from page when widget is not in page anymore', function() {
      var page = {
        assets: [{ id: 'anAsset', componentId: 'aWidgget' }],
        rows: []
      };
      spyOn(whiteboardService, 'contains').and.returnValue(false);
      spyOn(pageRepo, 'load').and.returnValue($q.when({ data: page }));
      editorService.initialize(pageRepo, 'person');
      $rootScope.$apply();

      editorService.removeAssetsFromPage({ id: 'aWidgget' });

      expect(page.assets).toEqual([]);
    });

    it('should not remove widget assets from page when widget still exists in page', function() {
      var page = {
        assets: [{ id: 'anAsset', componentId: 'aWidgget' }],
        rows: []
      };
      spyOn(whiteboardService, 'contains').and.returnValue(true);
      spyOn(pageRepo, 'load').and.returnValue($q.when({ data: page }));
      editorService.initialize(pageRepo, 'person');
      $rootScope.$apply();

      editorService.removeAssetsFromPage({ id: 'aWidgget' });

      expect(page.assets).toContain({ id: 'anAsset', componentId: 'aWidgget' });
    });
  });
})();
