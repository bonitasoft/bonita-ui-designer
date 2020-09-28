describe('autocomplete', function () {

  beforeEach(function () {
    browser.get('/bonita/preview/page/no-app-selected/autocomplete/');
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

  const labels = () => {
    return $$('.dropdown-menu a')
      .map(function (item) {
        return item.getText();
      });
  };

  describe('async', () => {
    // see https://bonitasoft.atlassian.net/browse/BS-16696
    it('should display the right suggestions', () => {
      const input = $('.test-async input');
      input.clear().sendKeys('walt');
      expect(labels()).toEqual([ 'walter.bates' ]);

      input.sendKeys(protractor.Key.BACK_SPACE);
      expect(labels()).toEqual([ 'walter.bates', 'thomas.wallis' ]);
    });

    it('should return the returned key of selection', () => {
      const input = $('.test-async-with-returnedKey input');
      input.clear().sendKeys('walter');
      var values = $$('.dropdown-menu a');
      values.get(0).click();

      var p = $('.test-async-with-returnedKey p');
      expect(p.getText()).toContain('4');
    });

    it('should return the full selected object if there is no returned key', () => {
      const input = $('.test-async-without-returnedKey input');
      input.clear().sendKeys('walter');
      var values = $$('.dropdown-menu a');
      values.get(0).click();

      var p = $('.test-async-without-returnedKey p');
      expect(p.getText()).toContain('walter.bates');
      expect(p.getText()).toContain('4');
    });
  });
});
