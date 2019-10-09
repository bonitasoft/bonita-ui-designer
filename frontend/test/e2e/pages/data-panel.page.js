(function() {
  'use strict';

  var DataSideBar = function() {
    this.sidebar = element(by.css('.BottomPanel'));
  };

  module.exports = DataSideBar;

  function dataElementToObject(dataElement) {
    return {
      name: dataElement.element(by.exactBinding('variable.name')).getText(),
      type: dataElement.element(by.css('.VariablesTable-type')).getText(),
      value: dataElement.element(by.exactBinding('displayValue(variable)')).getText()
    };
  }

  var dataMethod = {
    filter: function(filterPattern) {
      this.sidebar.element(by.model('search.value')).clear().sendKeys(filterPattern);
    },

    clearFilter: function() {
      this.sidebar.element(by.css('.Search-clearButton')).click();
    },

    addData: function(name, type, value) {

      this.addButton.click();

      this.name = name;
      this.type = type;

      if (type === 'Javascript expression' || type === 'JSON') {
        this.setAceValue(value);
      } else {
        this.value = value;
      }

      $('.modal-footer').element(by.buttonText('Save')).click();
    },

    setAceValue: function(value) {
      var formGroup = $('.modal-dialog .form-group--data');
      browser.actions().click(formGroup.element(by.css('.ace_content'))).perform();

      var textarea = formGroup.element(by.css('textarea'));
      textarea.sendKeys(protractor.Key.chord(protractor.Key.CONTROL, protractor.Key.ALT, protractor.Key.SHIFT,  'd'));
      textarea.sendKeys(value);
    },

    editData: function(name) {
      this.lines
        .filter(line => line.getText().then(text => text.indexOf(name) > -1))
        .then(lines => lines[0].element(by.css('.update-data')).click());
    },

    deleteData: function(index) {
      this.lines.get(index).element(by.css('.delete-data')).click();
      element(by.css('#confirm-delete')).click();
    },

    getData: function(index) {
      let data = this.lines.get(index);
      return dataElementToObject(data);
    },

    getDataByName: function(name) {
      let data = this.lines
        .filter(line => line.getText().then(text => text.indexOf(name) > -1))
        .first();
      return dataElementToObject(data);
    },

    sortByName: function() {
      element(by.cssContainingText('.VariablesTable thead th a', 'Name')).click();
    },

    sortByType: function() {
      element(by.cssContainingText('.VariablesTable thead th a', 'Type')).click();
    }
  };

  DataSideBar.prototype = Object.create(dataMethod, {

    name: {
      get: function() {
        return element(by.css('.modal-dialog')).element(by.model('newData.$$name'));
      },
      set: function(content) {
        element(by.css('.modal-dialog')).element(by.model('newData.$$name')).clear().sendKeys(content);
      }
    },

    type: {
      get: function() {
        return element(by.css('.modal-dialog')).element(by.model('newData.type'));
      },
      set: function(type) {
        $('.modal-dialog .form-group select option[label="' + type + '"]').click();
      }
    },

    value: {
      get: function() {
        return element(by.css('.modal-dialog')).element(by.model('newData.displayValue'));
      },
      set: function(content) {
        element(by.css('.modal-dialog')).element(by.model('newData.displayValue')).clear().sendKeys(content);
      }
    },

    addButton: {
      get: function() {
        return this.sidebar.element(by.buttonText('Create a new variable'));
      }
    },

    lines: {
      get: function() {
        return this.sidebar.all(by.css('.VariablesTable tbody tr'));
      }
    },

    popupExpandBtn: {
      get: function() {
        return $('.modal-content .DataPanel-expandableValue button');
      }
    },

    popupSaveBtn: {
      get: function() {
        return $('.modal-footer').element(by.buttonText('Save'));
      }
    }

  });

})();
