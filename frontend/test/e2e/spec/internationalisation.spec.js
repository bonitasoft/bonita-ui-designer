describe('i18n', function() {
  it('should translate in english by default', function() {
    browser.get('#/');
    expect($('.HomeCreate').getText()).toBe('Create');
  });

  it('should translate in english if the language is not recognized', function() {
    browser.get('#/unknown/home');
    expect($('.HomeCreate').getText()).toBe('Create');
  });

  it('should change when url is updated', function() {
    browser.get('#/fr/home');
    expect($('.HomeCreate').getText()).toBe('Créer');
  });
});
