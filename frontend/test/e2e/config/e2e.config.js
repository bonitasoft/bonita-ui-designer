(function() {

  'use strict';

  angular
    .module('bonitasoft.designer.e2e')
    .config(function($compileProvider) {
      // the 1.3 optimization needs to be disable for the e2e tests
      $compileProvider.debugInfoEnabled(true);
    })
    .run(function($httpBackend, e2ehelper, pages, widgets) {

      /********************************************************************************************************
       *                                          MISCELLANEOUS
       * ******************************************************************************************************/
      $httpBackend.whenGET(/^partials\//).passThrough();

      // I18n
      $httpBackend.whenGET(/i18n\/.*-fr-FR.json/).respond({'fr': {'Create': 'Créer'}});

      /********************************************************************************************************
       *                                            WIDGETS
       * ******************************************************************************************************/
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
        // get all (light representation)
      $httpBackend.whenGET('rest/pages').respond(function() {
        var response = pages.map(({id, name, type, lastUpdate, favorite}) => ({id, name, type, lastUpdate, favorite}));
        return [200, response, {}];
      });

      // get by id
      $httpBackend.whenGET(/rest\/pages\/.*/).respond(function(method, url) {
        var pageId = e2ehelper.lastChunk(url);
        var page = e2ehelper.getById(pages, pageId);
        return [200, page, {}];
      });

      // create new page
      $httpBackend.whenPOST('rest/pages?duplicata=person-page').respond(function(method, url, data) {
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

      $httpBackend.whenDELETE('rest/pages/person-page').respond(200);

      // create/update person assets
      $httpBackend.whenPOST('rest/pages/person-page/assets').respond(function(method, url, data) {
        var asset = angular.fromJson(data);
        asset.id = asset.id || e2ehelper.uuid();
        return [201, asset, {}];
      });


      /********************************************************************************************************
       *                                            EXPORT
       * ******************************************************************************************************/
      $httpBackend.whenGET('export/page/person-page').respond(200);

    });

})();
