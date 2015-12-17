describe('repeat container', function() {

  /**
   * This test repeats a container over a collection.
   * Each container contains an input and a paragraph, bound to the item.
   */
  beforeEach(function() {
    browser.get('/designer/preview/page/repeatContainer/');
  });

  it('should display 1 containers with 2 paragraphs and 2 inputs with item value', function() {
    var paragraphs = $$('p').filter(function(elem) {
      return elem.isDisplayed();
    });
    expect(paragraphs.count()).toBe(3);
    expect(paragraphs.get(0).getText()).toBe('hello');
    expect(paragraphs.get(1).getText()).toBe('designer');
    expect(paragraphs.get(2).getText()).toBe('["hello","designer"]');

    var inputs = $$('input').filter(function(elem) {
      return elem.isDisplayed();
    });
    expect(inputs.count()).toBe(2);
    expect(inputs.get(0).getAttribute('value')).toBe('hello');
    expect(inputs.get(1).getAttribute('value')).toBe('designer');
  });

  it('should update item everywhere when modified', function() {
    var inputs = $$('input').filter(function(elem) {
      return elem.isDisplayed();
    });

    // when updating the first collection item
    inputs.get(0).clear();
    inputs.get(0).sendKeys('ola');

    var paragraphs = $$('p').filter(function(elem) {
      return elem.isDisplayed();
    });
    expect(paragraphs.count()).toBe(3);
    expect(paragraphs.get(0).getText()).toBe('ola');
    expect(paragraphs.get(1).getText()).toBe('designer');
    expect(paragraphs.get(2).getText()).toBe('["ola","designer"]');

    expect(inputs.count()).toBe(2);
    expect(inputs.get(0).getAttribute('value')).toBe('ola');
    expect(inputs.get(1).getAttribute('value')).toBe('designer');
  });

});
