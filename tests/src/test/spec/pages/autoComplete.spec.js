describe('autocomplete', function () {

  beforeEach(function () {
    browser.get('/designer/preview/page/autocomplete/');
  });

  describe('simple list', function() {
    var input, p;

    beforeEach(function() {
      input = $('.test-simple input');
      p = $('.test-simple p');
    });

    it('should allow to pick a suggestion dropdownlist and update the value', function() {
      input.sendKeys('n');

      var values = $$('.dropdown-menu a');
      expect(values.count()).toBe(2);

      values.get(0).click();
      expect(p.getText()).toContain('London');

    });
  });

  describe('Object list', function() {
    var input, p;

    beforeEach(function() {
      input = $('.test-object input');
      p = $('.test-object p');
    });

    it('should display the correct value', function() {
      expect(p.getText()).toContain('{ "name": "Paul" }');
      expect(input.getAttribute('value')).toContain('Paul');
    });

    it('should display the correct suggestions', function() {
      input.clear().sendKeys('a');

      var values = $$('.dropdown-menu a');
      var labels = values.map(function(item) {
        return item.getText();
      });
      expect(values.count()).toBe(3);
      expect(labels).toEqual(['Paul', 'Hokusai', 'Pablo']);
    });

    it('should update the value when select a suggestion', function() {
      input.clear().sendKeys('a');

      var values = $$('.dropdown-menu a');
      values.get(1).click();
      expect(p.getText()).toContain('Hokusai');
    });
  });
});
