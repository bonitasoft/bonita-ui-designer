(function() {
  'use strict';

  describe('editor service', function() {
    var $rootScope, $q, widgetRepo, pageRepo, editorService, alerts, components, whiteboardComponentWrapper,
      whiteboardService, modalContainerStructureMockJSON, dataManagementRepoMock, migration, fragmentRepo;

    var labelWidget = {
      id: 'label',
      custom: false,
      type: 'widget'
    };

    var inputwidget = {
      id: 'pbInput',
      custom: false,
      type: 'widget'
    };

    var uidLabelWidget = {
      id: 'uidLabel',
      custom: false,
      type: 'widget',
      technology: 'web_component'
    };

    var uidInputwidget = {
      id: 'uidInput',
      custom: false,
      type: 'widget',
      technology: 'web_component'
    };

    let containers = [
      {
        id: 'pbContainer',
        name: 'Container',
        type: 'container'
      },
      {
        id: 'pbTabsContainer',
        name: 'Tabs container',
        type: 'container'
      },
      {
        id: 'pbFormContainer',
        name: 'Form container',
        type: 'container'
      },
      {
        id: 'pbModalContainer',
        name: 'Modal container',
        type: 'container'
      }
    ];

    let dataManagementWidgets =  [{
      id: 'Declarant',
      name: 'Declarant',
      custom: false,
      type: 'model'
    },{
      id: 'Declaration',
      name: 'Declaration',
      custom: false,
      type: 'model'
    }];

    var json = {
      'data': {
        'id': 'person',
        'name': 'person page',
        'rows': [
          [
            {
              'id':'pbContainer',
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
                    'id':'pbTabsContainer',
                    'type': 'tabsContainer',
                    'dimension': {'xs': 12},
                    'tabList': [
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
              'id':'pbFormContainer',
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

    var emptyJson = {
      'data': {
        'id': 'person',
        'name': 'person page',
        'rows': []
      }
    };

    let fragmentDef = {
      id: 'f1',
      type: 'fragment',
      name: 'foo',
      rows: [
        [
          {
            type: 'component',
            id: 'label'
          }
        ]
      ]
    };

    let jsonFragment = {
      'data': {
        'rows': [
          [
            {
              'type': 'component',
              'dimension': { 'xs': 12, 'sm': null, 'md': null, 'lg': null },
              'id': 'label',
              'propertyValues': { 'initialValue': null, 'placeholder': null, 'required': false, 'maxlength': null, 'label': 'First name', 'labelPosition': 'left', 'labelWidth': 4 }
            }
          ]
        ],
        'id': 'f1',
        'name': 'name'
      }
    };

    beforeEach(angular.mock.module('bonitasoft.designer.editor', 'modalContainerStructureMock' ));

    beforeEach(inject(function($injector) {
      modalContainerStructureMockJSON = $injector.get('modalContainerStructureMockJSON');
      $rootScope = $injector.get('$rootScope');
      $q = $injector.get('$q');

      widgetRepo = $injector.get('widgetRepo');
      pageRepo = $injector.get('pageRepo');
      fragmentRepo = $injector.get('fragmentRepo');
      dataManagementRepoMock = $injector.get('dataManagementRepo');

      editorService = $injector.get('editorService');
      components = $injector.get('components');
      whiteboardComponentWrapper = $injector.get('whiteboardComponentWrapper');
      alerts = $injector.get('alerts');
      whiteboardService = $injector.get('whiteboardService');
      migration = $injector.get('migration');
      spyOn(fragmentRepo, 'load').and.returnValue($q.when(jsonFragment));
      spyOn(fragmentRepo, 'all').and.returnValue($q.when({ data: [fragmentDef] }));
      spyOn(fragmentRepo, 'allNotUsingElement').and.returnValue($q.when({ data: [fragmentDef] }));

      spyOn(widgetRepo, 'angularJs').and.returnValue($q.when(containers.concat([labelWidget, inputwidget])));
      spyOn(widgetRepo, 'webComponents').and.returnValue($q.when(containers.concat([uidLabelWidget, uidInputwidget])));

      spyOn(dataManagementRepoMock, 'getDataObjects').and.returnValue($q.when({error: false, objects: dataManagementWidgets}));
      spyOn(alerts, 'addError');
      spyOn(fragmentRepo, 'migrate').and.returnValue($q.when({}));
      spyOn(pageRepo, 'migrate').and.returnValue($q.when({}));
      spyOn(pageRepo, 'migrationStatus').and.returnValue($q.when({data: {migration: false, compatible: true}}));
    }));

    beforeEach(inject(function (pageElementFactory, components) {
      spyOn(components, 'getById').and.returnValue({
        component: {
          id: 'pbTabContainer'
        }
      });
    }));

    it('should initialize a page', function() {
      let page = {};
      spyOn(migration, 'handleMigrationStatus').and.returnValue($q.when());
      spyOn(migration, 'handleMigrationNotif');
      spyOn(pageRepo, 'load').and.returnValue($q.when(json));
      editorService.initialize(pageRepo, 'person')
        .then(function(data) {
          page = data;
        });

      $rootScope.$apply();

      expect(pageRepo.migrate).not.toHaveBeenCalled();

      expect(page.rows[0][0].$$id).toBe('pbContainer-0');
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
      expect(tabsContainer.$$id).toBe('pbTabsContainer-0');
      expect(tabsContainer.$$widget.name).toBe('Tabs container');

      var formContainer = page.rows[1][0];
      expect(formContainer.$$id).toBe('pbFormContainer-0');
      expect(formContainer.$$widget.name).toBe('Form container');
      expect(formContainer.$$parentContainerRow.container).toBe(page);
    });

    it('should initialize web component a page', function() {
      spyOn(migration, 'handleMigrationStatus').and.returnValue($q.when());
      spyOn(migration, 'handleMigrationNotif');
      spyOn(pageRepo, 'load').and.returnValue($q.when(emptyJson));
      editorService.initialize(pageRepo, 'person', 'v2')

      $rootScope.$apply();
      expect(widgetRepo.webComponents).toHaveBeenCalled();
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
      expect(components.register.calls.count()).toBe(5);
    });

    it('should add an alert if initialize failed', function() {
      let errorMessage = {};
      errorMessage.message = 'load failed';
      spyOn(migration, 'handleMigrationStatus').and.returnValue($q.when());
      spyOn(migration, 'handleMigrationNotif');
      spyOn(pageRepo, 'load').and.returnValue($q.reject(errorMessage));

      editorService.initialize(pageRepo, 'person');
      $rootScope.$apply();

      expect(alerts.addError).toHaveBeenCalled();
    });

    it('should add an alert if incompatible migration or migration error', function() {
      let errorMessage = {};
      errorMessage.message = 'incompatible or migration error';
      spyOn(migration, 'handleMigrationStatus').and.returnValue($q.reject(errorMessage));

      editorService.initialize(pageRepo, 'person');
      $rootScope.$apply();

      expect(alerts.addError).toHaveBeenCalled();
    });

    it('should remove widget assets from page when widget is not in page anymore', function() {
      var page = {
        assets: [{id: 'anAsset', componentId: 'aWidgget'}],
        rows: []
      };
      spyOn(whiteboardService, 'contains').and.returnValue(false);
      spyOn(pageRepo, 'load').and.returnValue($q.when({data: page}));
      spyOn(migration, 'handleMigrationStatus').and.returnValue($q.when());
      spyOn(migration, 'handleMigrationNotif');
      editorService.initialize(pageRepo, 'person');
      $rootScope.$apply();

      editorService.removeAssetsFromPage({id: 'aWidgget'});

      expect(page.assets).toEqual([]);
    });

    it('should not remove widget assets from page when widget still exists in page', function() {
      var page = {
        assets: [{id: 'anAsset', componentId: 'aWidgget'}],
        rows: []
      };
      spyOn(whiteboardService, 'contains').and.returnValue(true);
      spyOn(pageRepo, 'load').and.returnValue($q.when({data: page}));
      editorService.initialize(pageRepo, 'person');
      spyOn(migration, 'handleMigrationStatus').and.returnValue($q.when());
      spyOn(migration, 'handleMigrationNotif');
      $rootScope.$apply();

      editorService.removeAssetsFromPage({id: 'aWidgget'});

      expect(page.assets).toContain({id: 'anAsset', componentId: 'aWidgget'});
    });

    it('should initialize a page with a modal container', function () {
      var page = {};
      spyOn(pageRepo, 'load').and.returnValue($q.when(modalContainerStructureMockJSON));
      spyOn(migration, 'handleMigrationStatus').and.returnValue($q.when());
      spyOn(migration, 'handleMigrationNotif');
      editorService.initialize(pageRepo, 'person')
        .then(function (data) {
          page = data;
        });
      $rootScope.$apply();
      expect(page.rows[0][0].$$id).toBe('pbModalContainer-0');
      expect(page.rows[0][0].id).toBe('pbModalContainer');
      expect(page.rows[0][0].$$widget.name).toBe('Modal container');
      expect(page.rows[0][0].container.rows[0][0].$$id).toBe('component-0');
      expect(page.rows[0][0].container.rows[0][0].id).toBe('pbInput');
      expect(page.rows[0][0].container.rows[0][0].$$widget).toEqual(inputwidget);
    });

    it('should migrate a page before initializing', function() {
      spyOn(migration, 'handleMigrationStatus').and.returnValue($q.when());
      spyOn(migration, 'handleMigrationNotif');
      pageRepo.migrationStatus = jasmine.createSpy().and.returnValue($q.when({data: {migration: true, compatible: true}}));
      spyOn(pageRepo, 'load').and.returnValue($q.when(json));

      editorService.initialize(pageRepo, 'person');
      $rootScope.$apply();

      expect(pageRepo.migrate).toHaveBeenCalled();
      expect(migration.handleMigrationNotif).toHaveBeenCalled();
    });

    it('should not call migrate when user click on cancel in migration popup', function() {
      spyOn(migration, 'handleMigrationStatus').and.returnValue($q.reject('cancel'));
      spyOn(migration, 'handleMigrationNotif');
      pageRepo.migrationStatus = jasmine.createSpy().and.returnValue($q.when({data: {migration: true, compatible: true}}));
      spyOn(pageRepo, 'load').and.returnValue($q.when(json));

      editorService.initialize(pageRepo, 'person');

      $rootScope.$apply();

      expect(pageRepo.migrate).not.toHaveBeenCalled();
      expect(migration.handleMigrationNotif).not.toHaveBeenCalled();
    });

    it('should initialize a fragment', function() {
      spyOn(fragmentRepo, 'migrationStatus').and.returnValue($q.when({data: {migration: false, compatible: true}}));
      var fragment = {};
      editorService.initialize(fragmentRepo, 'name')
        .then(function(data) {
          fragment = data;
        });
      $rootScope.$apply();

      expect(fragment.rows[0][0].$$id).toBe('component-0');
    });
  });
})();
