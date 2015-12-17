describe('select', function() {

  /**
   * The test sets up a input, with inputs bound to its properties.
   * We can play with its visibility, its value, its label display, etc...
   */
  beforeEach(function() {
    browser.get('/designer/preview/page/select/');
  });

  it('should manage contingency', function() {
    var select = $$('select');
    var countries = select.get(0).$$('option');
    // should have a placeholder and two countries
    expect(countries.get(0).getText()).toContain('Select a country');
    expect(countries.get(1).getText()).toEqual('France');
    expect(countries.get(2).getText()).toEqual('Spain');

    var cities = select.get(1).$$('option');
    // should have nothing now expect placeholder
    expect(cities.get(0).getText()).toContain('Select a city');
    expect(cities.count()).toBe(1);

    // select France country
    countries.get(1).click();
    // cities should have options
    cities = select.get(1).$$('option');
    expect(cities.get(1).getText()).toEqual('Paris');
    expect(cities.get(2).getText()).toEqual('Grenoble');
    expect(cities.get(3).getText()).toEqual('Charnècles');

    // select a city
    cities.get(3).click();
    expect($('pb-text p').getText()).toEqual('Charnècles');
  });

});
