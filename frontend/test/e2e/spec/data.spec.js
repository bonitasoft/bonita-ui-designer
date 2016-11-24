var PageEditor = require('../pages/editor.page.js');

describe('data panel', function() {

  describe('for page', function() {
    var dataPanel;

    beforeEach(function() {

      dataPanel = PageEditor.get('person-page').dataPanel();
      element(by.css('body')).allowAnimations(false);
    });

    it('should add a string data', function() {
      var nbData;
      dataPanel.lines.count().then(function(nb) {
        nbData = nb;
      });

      dataPanel.addData('aNewData', 'String', 'aNewValue');

      dataPanel.lines.count().then(function(nb) {
        expect(nb).toBe(nbData + 1);
      });

      let expectedData = dataPanel.getDataByName('aNewData');
      expect(expectedData.name).toBe('aNewData');
      expect(expectedData.type).toBe('String');
      expect(expectedData.value).toBe('aNewValue');
    });

    it('should add an Json data', function() {
      var nbData;
      dataPanel.lines.count().then(function(nb) {
        nbData = nb;
      });

      dataPanel.addData('aJson', 'JSON', '{"key": "foo"}');

      dataPanel.lines.count().then(function(nb) {
        expect(nb).toBe(nbData + 1);
      });

      let expectedData = dataPanel.getDataByName('aJson');
      expect(expectedData.name).toBe('aJson');
      expect(expectedData.type).toBe('JSON');
      expect(expectedData.value).toBe('{"key": "foo"}');
    });

    it('should add an Url data', function() {
      var nbData;
      dataPanel.lines.count().then(function(nb) {
        nbData = nb;
      });

      dataPanel.addData('aUrl', 'External API',  '{{base}}/bonita/test');

      dataPanel.lines.count().then(function(nb) {
        expect(nb).toBe(nbData + 1);
      });

      let expectedData = dataPanel.getDataByName('aUrl');
      expect(expectedData.name).toBe('aUrl');
      expect(expectedData.type).toBe('External API');
      expect(expectedData.value).toBe('{{base}}/bonita/test');
    });

    it('should add a Javascript expression data', function() {
      var nbData;
      dataPanel.lines.count().then(function(nb) {
        nbData = nb;
      });

      dataPanel.addData('anExpression', 'Javascript expression', 'return "test";');

      dataPanel.lines.count().then(function(nb) {
        expect(nb).toBe(nbData + 1);
      });

      let expectedData = dataPanel.getDataByName('anExpression');
      expect(expectedData.name).toBe('anExpression');
      expect(expectedData.type).toBe('Javascript expression');
      expect(expectedData.value).toBe('return "test";');
    });

    it('should delete a data', function() {
      var nbData;
      dataPanel.lines.count().then(function(nb) {
        nbData = nb;
      });

      dataPanel.deleteData(0);

      dataPanel.lines.count().then(function(nb) {
        expect(nb).toBe(nbData - 1);
      });
    });

    it('should filter data', function() {
      dataPanel.addData('aName', 'String', 'aValue');

      dataPanel.filter('aName');
      expect(dataPanel.lines.count()).toBe(1);

      dataPanel.clearFilter();
      expect(dataPanel.lines.count()).toBe(4);
    });

    it('should sort data by name or type', function() {
      // Default order is 'name'
      expect(dataPanel.getData(0).name).toBe('alreadyExistsData');
      expect(dataPanel.getData(1).name).toBe('jsonExample');
      expect(dataPanel.getData(2).name).toBe('urlExample');

      dataPanel.sortByName();
      expect(dataPanel.getData(0).name).toBe('urlExample');
      expect(dataPanel.getData(1).name).toBe('jsonExample');
      expect(dataPanel.getData(2).name).toBe('alreadyExistsData');

      dataPanel.sortByType();
      expect(dataPanel.getData(0).type).toBe('String');
      expect(dataPanel.getData(1).type).toBe('JSON');
      expect(dataPanel.getData(2).type).toBe('External API');

      dataPanel.sortByType();
      expect(dataPanel.getData(0).type).toBe('External API');
      expect(dataPanel.getData(1).type).toBe('JSON');
      expect(dataPanel.getData(2).type).toBe('String');
    });

    it('should expand value field for Json when button is clicked', function() {
      dataPanel.addButton.click();
      expect(dataPanel.popupExpandBtn.isPresent()).toBeFalsy();
      dataPanel.type = 'JSON';
      dataPanel.popupExpandBtn.click();
      expect(dataPanel.value.isDisplayed()).toBeTruthy();
      expect(dataPanel.name.isDisplayed()).toBeFalsy();
      expect(dataPanel.type.isDisplayed()).toBeFalsy();
      dataPanel.popupExpandBtn.click();
      expect(dataPanel.value.isDisplayed()).toBeTruthy();
      expect(dataPanel.name.isDisplayed()).toBeTruthy();
      expect(dataPanel.type.isDisplayed()).toBeTruthy();
      dataPanel.type = 'Javascript expression';
      dataPanel.popupExpandBtn.click();
      expect(dataPanel.value.isDisplayed()).toBeTruthy();
      expect(dataPanel.name.isDisplayed()).toBeFalsy();
      expect(dataPanel.type.isDisplayed()).toBeFalsy();
    });

    it('should not allow adding invalid Json', function() {
      dataPanel.addButton.click();
      dataPanel.name = 'aJson';
      dataPanel.type = 'JSON';
      dataPanel.setAceValue('zezez');
      expect(dataPanel.popupSaveBtn.isEnabled()).toBe(false);
    });

    it('should not allow adding empty url', function() {
      dataPanel.addButton.click();
      dataPanel.name = 'aUrl';
      dataPanel.type = 'External API';
      dataPanel.value = '';
      expect(dataPanel.popupSaveBtn.isEnabled()).toBe(false);
    });

    it('should not be possible to add data with same name than already existing one', function() {
      dataPanel.addButton.click();
      dataPanel.name = 'alreadyExistsData';
      dataPanel.value = 'aValue';
      expect(dataPanel.popupSaveBtn.isEnabled()).toBe(false);
    });

    it('should not be possible to add data with an invalid name', function() {
      dataPanel.addButton.click();

      dataPanel.name = 'invalid name';
      dataPanel.value = 'aValue';

      expect(dataPanel.popupSaveBtn.isEnabled()).toBe(false);

      dataPanel.name = '1data';
      dataPanel.value = 'aValue';

      expect(dataPanel.popupSaveBtn.isEnabled()).toBe(false);
    });

    it('should display an error message when invalid name is provided', function() {
      dataPanel.addButton.click();
      dataPanel.name = 'already ExistsData';
      dataPanel.value = 'aValue';
      expect($('.modal-dialog .form-group:first-child input + p').isDisplayed()).toBeTruthy();
    });

    it('should allow modifying existing data value and rollbacking', function() {
      dataPanel.editData('alreadyExistsData');
      dataPanel.value = 'foo';
      $('.modal-footer .btn-link').click();
      expect(dataPanel.getDataByName('alreadyExistsData').value).toEqual('aValue');
    });

    it('should allow modifying existing data value and confirming', function() {
      dataPanel.editData('alreadyExistsData');
      dataPanel.value = 'foo';
      dataPanel.popupSaveBtn.click();
      expect(dataPanel.getDataByName('alreadyExistsData').value).toEqual('foo');
    });

    it('should not allow confirming invalid URL', function() {
      dataPanel.editData('urlExample');
      dataPanel.value = '';
      expect(dataPanel.popupSaveBtn.isEnabled()).toBe(false);
    });

    it('should not allow confirming invalid Json', function() {
      dataPanel.editData('jsonExample');
      dataPanel.type = 'JSON';
      dataPanel.setAceValue('');
      expect(dataPanel.popupSaveBtn.isEnabled()).toBe(false);
    });

    it('should open help popup',  function() {
      $('.btn-data--help').click();

      expect($('.modal-header .modal-title').getText()).toBe('Help');
    });
  });
});
