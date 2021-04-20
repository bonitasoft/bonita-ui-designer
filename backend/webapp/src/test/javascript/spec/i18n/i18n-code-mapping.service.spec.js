describe('i18n code mapping', function () {
  var i18nCodeMapping;

  beforeEach(module('bonitasoft.ui.i18n'));

  beforeEach(inject(function (_i18nCodeMapping_) {
    i18nCodeMapping= _i18nCodeMapping_;
  }));

  it('should choose exact code first if exists', function() {
    expect(i18nCodeMapping.get('fr', ['fr-FR', 'fr_FR', 'fr', 'en-US'])).toBe('fr');
  });

  it('should get corresponding hyphen separated code for two letter code ignoring case', function() {
    expect(i18nCodeMapping.get('fr', ['fr-FR', 'fr_FR', 'en-US'])).toBe('fr-FR');
    expect(i18nCodeMapping.get('fr', ['fr-fr', 'fr_FR', 'en-US'])).toBe('fr-fr');
  });

  it('should get corresponding underscore separated code for two letter code ignoring case', function() {
    expect(i18nCodeMapping.get('fr', ['fr_FR', 'en-US'])).toBe('fr_FR');
    expect(i18nCodeMapping.get('fr', ['fr_fr', 'en-US'])).toBe('fr_fr');
  });

  it('should get corresponding two letter code for hyphen/underscore separated code ignoring case', function() {
    expect(i18nCodeMapping.get('fr-BE', ['fr', 'it'])).toBe('fr');
    expect(i18nCodeMapping.get('fr_BE', ['fr', 'it'])).toBe('fr');
    expect(i18nCodeMapping.get('fr_BE', ['us', 'it'])).toBe('fr_BE');
  });

  it('should get corresponding code for hyphen/underscore code', function() {
    expect(i18nCodeMapping.get('fr-FR', ['fr_FR', 'en_US'])).toBe('fr_FR');
    expect(i18nCodeMapping.get('fr_FR', ['fr-FR', 'en-US'])).toBe('fr-FR');
  });

  it('should get corresponding code for japanese code', function() {
    expect(i18nCodeMapping.get('ja', ['fr_FR', 'en_US', 'ja-JP'])).toBe('ja-JP');
    expect(i18nCodeMapping.get('ja-JP', ['fr_FR', 'en_US', 'ja-JP'])).toBe('ja-JP');
  });

  it('should get return code in argument otherwise', function() {
    expect(i18nCodeMapping.get('unknown', ['fr_FR', 'en_US'])).toBe('unknown');
  });

});
