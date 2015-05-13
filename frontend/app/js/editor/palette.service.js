/**
 * Components Service manage ui-designer components
 * handle registration and initialisation.
 *
 * Create a new scope with a properties object derived from user entered propertyValues.
 * This allow to bind propertyValues to widget properties and keep a WYSWYG approach in editor while editing widget properties
 */
angular.module('pb.services')
  .service('paletteService', function() {

    'use strict';

    var componentsMap = {};

    this.reset = function() {
      componentsMap = {};
    };

    this.getSections = function() {
      var sections = Object.keys(componentsMap).reduce(function(sections, item){
        var sectionName = componentsMap[item].sectionName;
        sections[sectionName] = sections[sectionName] || {
          name: sectionName,
          order: componentsMap[item].sectionOrder,
          widgets: []
        };

        sections[sectionName].widgets = sections[sectionName].widgets.concat(componentsMap[item]);
        return sections;
      }, {});

      return Object.keys(sections).map(function (key) {
        return sections[key];
      });
    };

    this.register = function(items) {
      componentsMap = items.reduce(function(components, item){
        components[item.component.id] = item;
        return components;
      }, componentsMap);
    };

    this.init = function(component, parentRow) {
      // container have no id, only a type
      var id = component.id || component.type;

      if (!componentsMap.hasOwnProperty(id)) {
        throw new Error('Component ' + id + ' has not been registered');
      }
      var fnInit = componentsMap[id].init;
      fnInit(component, parentRow);
    };

  });
