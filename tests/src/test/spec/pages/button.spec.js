describe('button', function () {

  function getPeoples() {
    return $$('pb-table tbody td');
  }

  beforeEach(function () {
    browser.get('/designer/preview/page/buttonAddRemove/');

    var peoples = getPeoples();
    expect(peoples.count()).toBe(4);
    expect(peoples.get(0).getText()).toEqual('colin');
    expect(peoples.get(1).getText()).toEqual('vincent');
    expect(peoples.get(2).getText()).toEqual('guillaume');
    expect(peoples.get(3).getText()).toEqual('lionel');
  });

  describe('common properties', function() {
    it('should disable a button', function() {
      var button = element(by.buttonText('Disabled Button'));
      expect(button.isEnabled()).toBe(false);
    });

    it('should hide a button', function() {
      var button = element(by.buttonText('Hidden Button'));
      expect(button.isPresent()).toBe(false);
    });

  });

  describe('button remove action', function () {

    it('should remove first element of a collection', function () {
      element(by.buttonText('Remove First')).click();

      var peoples = getPeoples();

      expect(peoples.count()).toBe(3);
      expect(peoples.get(0).getText()).toEqual('vincent');


      element(by.buttonText('Remove First')).click();

      var peoples = getPeoples();

      expect(peoples.count()).toBe(2);
      expect(peoples.get(0).getText()).toEqual('guillaume');
    });

    it('should remove last element of a collection', function () {

      element(by.buttonText('Remove Last')).click();

      var peoples = getPeoples();
      expect(peoples.count()).toBe(3);
      expect(peoples.get(0).getText()).toEqual('colin');

      element(by.buttonText('Remove Last')).click();

      peoples = getPeoples();
      expect(peoples.count()).toBe(2);
      expect(peoples.get(0).getText()).toEqual('colin');
    });
  });

  describe('button add action', function () {

    it('should add first element of a collection', function () {
      element(by.buttonText('Add First')).click();

      var peoples = getPeoples();
      expect(peoples.count()).toBe(5);
      expect(peoples.get(0).getText()).toEqual('john doé');
    });

    it('should remove last element of a collection', function () {

      element(by.buttonText('Add Last')).click();

      var peoples = getPeoples();
      expect(peoples.count()).toBe(5);
      expect(peoples.get(4).getText()).toEqual('Hélène');
    });
  });
});
