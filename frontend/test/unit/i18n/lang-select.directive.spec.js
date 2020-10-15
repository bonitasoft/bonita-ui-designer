describe('lang select directive', function () {

  var element, scope, $rootScope, controller, i18n;

  beforeEach(angular.mock.module('bonitasoft.designer.i18n'));

  beforeEach(inject(function ($compile, _$rootScope_, _i18n_) {
    $rootScope = _$rootScope_;
    i18n = _i18n_;
    scope = $rootScope.$new();
    scope.refresh = jasmine.createSpy('refresh');

    element = $compile('<language-picker on-change="refresh()"></language-picker>')(scope);
    scope.$apply();
    controller = element.controller('languagePicker');
  }));

  it('should select language and call function bind on on-change', function () {
    spyOn(i18n, 'selectLanguage');

    controller.chooseLang('de-DE');

    expect(i18n.selectLanguage).toHaveBeenCalledWith('de-DE');
    expect(controller.lang).toBe('de-DE');
    expect(scope.refresh).toHaveBeenCalled();
  });

  it('should refresh available langs and selected lang when i18n service is refreshed', function () {

    i18n.refresh(['fr-FR', 'es-ES'], 'fr-FR');

    expect(controller.lang).toBe('fr-FR');
    expect(controller.langs).toEqual(['Default', 'fr-FR', 'es-ES']);
  });

  it('should select default land when cookie is set but lang not available in langs', function() {

    i18n.refresh(['fr-FR', 'es-ES'], 'notexisting');

    expect(controller.lang).toBe('Default');
  });

  it('should remove cookie used for preview but the one used by portal on destroy', () => {
    spyOn(i18n, 'cleanLanguage');

    scope.$destroy();

    expect(i18n.cleanLanguage).toHaveBeenCalled();
  });

});
