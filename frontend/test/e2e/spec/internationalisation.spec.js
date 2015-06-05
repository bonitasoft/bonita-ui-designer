describe('i18n', function() {
  it('should translate in english by default', function() {
    browser.get('#/');
    expect($$('#pages legend').first().getText()).toBe('New page');
  });

  it('should translate in english if the language is not recognized', function() {
    browser.get('#/unknown/home');
    expect($$('#pages legend').first().getText()).toBe('New page');
  });

  it('should change when url is updated', function() {
    browser.get('#/fr/home');
    expect($$('#pages legend').first().getText()).toBe('Nouvelle page');
  });
});
