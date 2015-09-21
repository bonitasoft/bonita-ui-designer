(function () {
   'use strict';

    describe('whiteboard', function() {
    var $rootScope, $httpBackend, $q, widgetRepo, pageRepo, whiteboard, alerts, paletteService, componentFactory;

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
              'dimension': {'xs': 12},
              'rows': [
                [
                  {
                    'type': 'component',
                    'id': 'label',
                    'dimension': {'xs': 12},
                    'propertyValues': {'text': 'label 1', 'alignment': 'left'}
                  }
                ],
                [
                  {
                    'type': 'component',
                    'id': 'label',
                    'dimension': {'xs': 12},
                    'propertyValues': {'text': 'label 2', 'alignment': 'left'}
                  }
                ],
                [
                  {
                    'type': 'tabsContainer',
                    'dimension': {'xs': 12},
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
                'rows': [
                ]
              },
              'name': '',
              'method': '',
              'action': null
            }
          ]
        ]
      }
    };

    beforeEach(angular.mock.module('bonitasoft.designer.common.services', 'bonitasoft.designer.common.repositories', 'bonitasoft.designer.services', 'bonitasoft.designer.factories', 'bonitasoft.designer.editor.common', 'bonitasoft.designer.editor.palette'));
    beforeEach(inject(function($injector) {
      $rootScope = $injector.get('$rootScope');
      $httpBackend = $injector.get('$httpBackend');
      $q = $injector.get('$q');

      widgetRepo = $injector.get('widgetRepo');
      pageRepo = $injector.get('pageRepo');

      whiteboard = $injector.get('whiteboard');
      paletteService = $injector.get('paletteService');
      componentFactory = $injector.get('componentFactory');
      alerts = $injector.get('alerts');

      spyOn(widgetRepo, 'all').and.returnValue($q.when({ data: [labelWidget] }));
      spyOn(alerts, 'addError');
    }));


    it('should initialize a page', function() {
      var page = {};
      spyOn(pageRepo, 'load').and.returnValue($q.when(json));
      whiteboard.initialize(pageRepo, 'person')
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

      whiteboard.addPalette('foo', spy);
      whiteboard.initialize(pageRepo, 'person');
      $rootScope.$apply();

      expect(spy).toHaveBeenCalled();
    });

    it('should init palette', function() {

      spyOn(paletteService, 'register');
      spyOn(paletteService, 'reset');
      spyOn(componentFactory, 'initializePage').and.returnValue({});
      spyOn(pageRepo, 'load').and.returnValue($q.when(json));

      whiteboard.initialize(pageRepo, 'person');
      $rootScope.$apply();

      expect(paletteService.reset).toHaveBeenCalled();
      expect(paletteService.reset.calls.count()).toBe(1);

      expect(paletteService.register).toHaveBeenCalled();
      expect(paletteService.register.calls.count()).toBe(3);
    });

    it('should add an alert if initialize failed', function() {
      spyOn(pageRepo, 'load').and.returnValue($q.reject('load failed'));

      whiteboard.initialize(pageRepo, 'person');
      $rootScope.$apply();

      expect(alerts.addError).toHaveBeenCalled();
    });
  });
})();
