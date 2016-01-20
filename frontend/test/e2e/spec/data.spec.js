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

      dataPanel.addData('aName', 'String', 'aValue');

      dataPanel.lines.count().then(function(nb) {
        expect(nb).toBe(nbData + 1);
      });

      expect(dataPanel.lines.first().element(by.exactBinding('data.value')).getText()).toBe('aValue');
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

      expect(dataPanel.lines.last().element(by.exactBinding('data.value')).getText()).toBe('{"key": "foo"}');
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

      expect(dataPanel.lines.last().element(by.exactBinding('data.value')).getText()).toBe('{{base}}/bonita/test');
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

      expect(dataPanel.lines.last().element(by.exactBinding('data.value')).getText()).toBe('return "test";');
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
    });

    it('should clear the data filter', function() {
      dataPanel.addData('aName', 'String', 'aValue');
      dataPanel.filter('aName');
      dataPanel.clearFilter();
      expect(dataPanel.lines.count()).toBe(4);
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
      dataPanel.editData(0);
      dataPanel.value = 'foo';
      $('.modal-footer .btn-link').click();
      expect(dataPanel.lines.first().element(by.exactBinding('data.value')).getText()).toEqual('aValue');
    });

    it('should allow modifying existing data value and confirming', function() {
      dataPanel.editData(0);
      dataPanel.value = 'foo';
      dataPanel.popupSaveBtn.click();
      expect(dataPanel.lines.first().element(by.exactBinding('data.value')).getText()).toEqual('foo');
    });

    it('should not allow confirming invalid URL', function() {
      dataPanel.editData(2);
      dataPanel.value = '';
      expect(dataPanel.popupSaveBtn.isEnabled()).toBe(false);
    });

    it('should not allow confirming invalid Json', function() {
      dataPanel.editData(1);
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
