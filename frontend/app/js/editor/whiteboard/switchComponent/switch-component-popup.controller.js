/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
(function() {

  class SwitchComponentPopupController {
    constructor($uibModalInstance, widgets, widgetFrom, properties, dictionary) {
      this.$uibModalInstance = $uibModalInstance;
      this.widgets = widgets;
      this.widgetFrom = widgetFrom;
      this.propertiesService = properties;
      this.selectedWidget = '';
      this.widgetsToDisplay = this.widgets.sort((a, b) => a.name.localeCompare(b.name)).filter(w => this._filterWidget(w)).map(item => {
        return { id: item.id, name: item.name, custom: item.custom };
      });
      this.propertiesFrom = this._extractFromProperties(this.widgetFrom);

      // Get dictionary to give it on webComponent
      let dictionaryValues = Object.values(dictionary);
      this.dictionary = this._attachDictionary(dictionaryValues[0]);
      this.propertiesIsDisplay = false;

    }

    _filterWidget(widget) {
      return widget.id !== this.widgetFrom.$$widget.id && (widget.type !== 'container' ||  widget.type !== 'data model') && widget.id !== 'pbTabContainer';
    }

    _attachDictionary(dictionary) {
      let dico = {};
      if (dictionary) {
        Object.keys(dictionary).forEach(key => {
          dico[key] = dictionary[key].$$noContext[0];
        });
      }
      return dico;
    }

    _extractFromProperties(widget) {
      let options = {};
      let widgetPropertyLabel = widget.$$widget.properties;
      Object.keys(widget.propertyValues).forEach(property => {
        let labelProperty = widgetPropertyLabel.filter(p => p.name === property);
        if (labelProperty.length > 0) {
          options[property] = widget.propertyValues[property];
          options[property].label = labelProperty[0].label;
        }
      });
      return { name: widget.$$widget.name, options: options };
    }

    getWidget(widgetId) {
      let widgets = this.widgets.filter(w => w.id === widgetId);
      let widget = widgets[0];
      widget = this.propertiesService.addCommonPropertiesTo(widget);
      let options = {};
      widget.properties.forEach(w => {
        options[w.name] = w;
      });

      return { id: widget.id, name: widget.name, options: options };
    }

    isClearButtonVisible() {
      return (this.selectedWidget || '').trim().length > 0;
    }

    clearValue() {
      this.selectedWidget = '';
    }

    isBtnPropertiesEnabled() {
      let selectedWidget = this.widgetsToDisplay.filter(w => w.name.toLowerCase() === this.selectedWidget.toLowerCase());
      return selectedWidget.length > 0;
    }

    showProperties() {
      if (!this.selectedWidget) {
        this.selectedWidget = '';
        return;
      }

      let selectedWidget = this.widgetsToDisplay.filter(w => w.name.toLowerCase() === this.selectedWidget.toLowerCase());
      if (selectedWidget.length > 0) {
        this.propertiesTo = this.getWidget(selectedWidget[0].id);
        this.propertiesIsDisplay = true;
      }
    }

    applyConfig() {
      let switchComponentConfig = document.getElementById('switch-component-config');
      this.$uibModalInstance.close({
        'mapping': switchComponentConfig.result,
        'dimension': this.widgetFrom.dimension
      }, this.widgetFrom);
    }

    reset() {
      let switchComponentConfig = document.getElementById('switch-component-config');
      switchComponentConfig.resetMapping();
    }
  }

  angular.module('bonitasoft.designer.editor.whiteboard').controller('SwitchComponentPopupController', SwitchComponentPopupController);
})();
