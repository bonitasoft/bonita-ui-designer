describe('textarea', function() {

  beforeEach(function() {
    browser.get('/designer/preview/page/textarea/');
  });

  it('should display a value and update it', function() {
    var textareas = $$('pb-textarea textarea');

    expect(textareas.get(0).getAttribute('value')).toBe("Here is the preloaded text");
    expect(textareas.get(1).getAttribute('value')).toBe("Here is the preloaded text");

    textareas.get(0).clear();
    textareas.get(0).sendKeys('This is the new value');

    expect(textareas.get(1).getAttribute('value')).toBe("This is the new value");
  });


});
