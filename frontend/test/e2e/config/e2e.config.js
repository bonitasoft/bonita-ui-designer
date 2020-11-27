(function() {

  'use strict';

  angular
    .module('bonitasoft.designer.e2e')
    .config(function($compileProvider) {
      // the 1.3 optimization needs to be disable for the e2e tests
      $compileProvider.debugInfoEnabled(true);
    })
    .run(function($httpBackend, $localStorage, e2ehelper, pages, widgets, assets, fragments) {

      /********************************************************************************************************
       *                                          LOCAL STORAGE
       * ******************************************************************************************************/
      $localStorage.bonitaUIDesigner = { doNotShowMigrationNotesAgain: true };

      /********************************************************************************************************
       *                                          MISCELLANEOUS
       * ******************************************************************************************************/
      $httpBackend.whenGET(/^partials\//).passThrough();

      // I18n
      $httpBackend.whenGET(/i18n\/.*-fr-FR.json/).respond({'fr': {'Create': 'Créer'}});

      $httpBackend.whenGET(/rest\/migration\/status\/page\/aPageToMigrate/).respond(() => { return [200,{'compatible': true, 'migration': true},{}];});
      $httpBackend.whenPUT(/rest\/migration\/page\/aPageToMigrate/).respond(() => { return [200,{ 'status': 'success', 'migrationStepReport':[] },{}];} );

      $httpBackend.whenGET(/rest\/migration\/status\/page\/aPageIncompatible/).respond(() => { return [200,{'compatible': false, 'migration': false},{}];});

      // Status
      $httpBackend.whenGET(/rest\/migration\/status\/.*\/.*/).respond(() => { return [200,{'compatible': true, 'migration': false},{}];});
      // Config
      $httpBackend.whenGET(/rest\/config/).respond(() => { return [200,{}];});

      // Migration
      $httpBackend.whenPUT(/rest\/migration\/.*\/.*/).respond(() => { return [200,{ 'comments': 'No migration is needed', 'status': 'none', 'migrationStepReport':[] },{}];} );

      /********************************************************************************************************
       *                                            WIDGETS
       * ******************************************************************************************************/
      // load an asset content
      $httpBackend.whenGET(/rest\/widgets\/.*\/assets\/.*\?format=text/).respond(function (method, url) {
        var filename = e2ehelper.lastChunk(url);
        return [200, assets[filename].content, {}];
      });

      // update an asset content
      $httpBackend.whenPOST(/rest\/widgets\/.*\/assets\/./).respond(function (method, url, data) {
        var file = data.get('file');
        var reader = new FileReader();
        reader.readAsText(file);
        reader.onloadend = function () {
          assets[file.name].content = reader.result;
        };
        return [200];
      });

        // create new widget property
      $httpBackend.whenPOST(/rest\/widgets\/.*\/properties/).respond(function(method, url, data) {
        var property = angular.fromJson(data);
        var widgetId = url.match(/rest\/widgets\/(.*)\/properties/)[1];
        var widget = e2ehelper.getById(widgets, widgetId);
        widget.properties.push(property);
        return [200, widget.properties, {}];
      });

      // update widget property
      $httpBackend.whenPUT(/rest\/widgets\/.*\/properties\/.*/).respond(function(method, url, data) {
        var updatedProperty = angular.fromJson(data);
        var urlMatches = url.match(/rest\/widgets\/(.*)\/properties\/(.*)/);
        var widgetId = urlMatches[1];
        var propertyName = urlMatches[2];
        var widget = e2ehelper.getById(widgets, widgetId);
        widget.properties = widget.properties.map(function(property) {
          if (property.name === propertyName) {
            return updatedProperty;
          }
          return property;
        });
        return [200, widget.properties, {}];
      });

      // delete widget property
      $httpBackend.whenDELETE(/rest\/widgets\/.*\/properties\/.*/).respond(function(method, url) {
        var urlMatches = url.match(/rest\/widgets\/(.*)\/properties\/(.*)/);
        var widgetId = urlMatches[1];
        var propertyName = urlMatches[2];
        var widget = e2ehelper.getById(widgets, widgetId);
        widget.properties = widget.properties.filter(function(property) {
          return property.name !== propertyName;
        });
        return [200, widget.properties, {}];
      });

      // get all
      $httpBackend.whenGET('rest/widgets').respond(widgets);

      // get all light representation
      $httpBackend.whenGET('rest/widgets?view=light').respond(function() {
        var response = widgets.map(({id, name, custom, lastUpdate, favorite}) => ({id, name, custom, lastUpdate, favorite}));
        return [200, response, {}];
      });

      // get by id
      $httpBackend.whenGET(/rest\/widgets\/.*/).respond(function(method, url) {
        var widgetId = e2ehelper.lastChunk(url);
        var widget = e2ehelper.getById(widgets, widgetId);
        return [200, widget, {}];
      });

      // create new widget
      $httpBackend.whenPOST('rest/widgets').respond(function(method, url, data) {
        var widget = angular.fromJson(data);
        widget.id = 'custom' + widget.name;
        widget.assets = [];
        widgets.push(widget);
        return [200, widget, {}];
      });

      // duplicate widget
      $httpBackend.whenPOST('rest/widgets?duplicata=customAwesomeWidget').respond(function(method, url, data) {
        var widget = angular.fromJson(data);
        widget.id = 'custom' + widget.name;
        widgets.push(widget);
        return [200, widget, {}];
      });

      // update widget
      $httpBackend.whenPUT(/rest\/widgets\/.*/).respond(200);

      /********************************************************************************************************
       *                                            PAGES
       * ******************************************************************************************************/

      // load an asset content
      $httpBackend.whenGET(/rest\/pages\/.*\/assets\/.*\?format=text/).respond(function (method, url) {
        var filename = e2ehelper.lastChunk(url);
        return [200, assets[filename].content, {}];
      });

      // update an asset content
      $httpBackend.whenPOST(/rest\/pages\/.*\/assets\/.+/).respond(function(method, url, data) {
        var file = data.get('file');
        var reader = new FileReader();
        reader.readAsText(file);
        reader.onloadend = function(){
          assets[file.name].content = reader.result;
        };
        return [200];
      });

      // get all (light representation)
      $httpBackend.whenGET('rest/pages').respond(function() {
        var response = pages.map(({id, name, type, lastUpdate, favorite, status}) => ({id, name, type, lastUpdate, favorite, status}));
        return [200, response, {}];
      });

      // get by id
      $httpBackend.whenGET(/rest\/pages\/.*/).respond(function(method, url) {
        var pageId = e2ehelper.lastChunk(url);
        var page = e2ehelper.getById(pages, pageId);
        return [200, page, {}];
      });

      // create new page
      $httpBackend.whenPOST('rest/pages?duplicata=personPage').respond(function(method, url, data) {
        var page = angular.fromJson(data);
        page.id = page.name;
        pages.push(page);
        return [201, page, {}];
      });

      // duplicate page
      $httpBackend.whenPOST('rest/pages').respond(function(method, url, data) {
        var page = angular.fromJson(data);
        page.id = page.name;
        pages.push(page);
        return [201, page, {}];
      });

      // update page
      $httpBackend.whenPUT(/rest\/pages\/.*/).respond(200);

      $httpBackend.whenDELETE('rest/pages/personPage').respond(200);

      // create/update person assets
      $httpBackend.whenPOST('rest/pages/personPage/assets').respond(function(method, url, data) {
        var asset = angular.fromJson(data);
        asset.id = asset.id || e2ehelper.uuid();
        return [201, asset, {}];
      });

      /********************************************************************************************************
       *                                            FRAGMENTS
       * ******************************************************************************************************/
      $httpBackend.whenGET('rest/fragments').respond(fragments);

      $httpBackend.whenGET(/rest\/fragments\?notUsedBy=.*/).respond(fragments);

      $httpBackend.whenGET('rest/fragments?view=light').respond(() => {
        var response = fragments.map(({id, name, type, lastUpdate, favorite}) => ({id, name, type, lastUpdate, favorite}));
        return [200, response, {}];
      });

      $httpBackend.whenPUT('rest/fragments/personFragment/name').respond(function(method, url, data) {
        fragments[0].name = data;
        return [200, fragments[0], {}];
      });

      $httpBackend.whenPOST('rest/fragments').respond(201, fragments[0]);

      $httpBackend.whenDELETE('rest/fragments/personFragment').respond(200);

      $httpBackend.whenGET('rest/fragments/personFragment').respond(fragments[0]);
      $httpBackend.whenGET('rest/fragments/fragInFragInFragFragment').respond(fragments[2]);

      $httpBackend.whenGET('export/fragment/personFragment').respond(200);


      /********************************************************************************************************
       *                                            EXPORT
       * ******************************************************************************************************/
      $httpBackend.whenGET('export/page/personPage').respond(200);

    });

})();
