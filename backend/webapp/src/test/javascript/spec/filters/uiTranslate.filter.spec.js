describe('uiTranslate filter', function () {

  beforeEach(module('bonitasoft.ui.filters'));

  var uiTranslateFilter;

  beforeEach(inject(function (_uiTranslateFilter_, gettextCatalog) {

    uiTranslateFilter = _uiTranslateFilter_;

    gettextCatalog.setCurrentLanguage('nl');
    gettextCatalog.setStrings("nl", {
      "Hello": "Hallo"
    });
  }));

  it('should translate a string passed through', function () {
    expect(uiTranslateFilter('Hello')).toBe('Hallo');
  });

});
