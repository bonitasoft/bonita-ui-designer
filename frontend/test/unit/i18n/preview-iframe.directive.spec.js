describe('preview iframe directive', function() {

  var scope, element, $rootScope, i18n, iframeCatalog, iframe;

  beforeEach(angular.mock.module('bonitasoft.designer.i18n'));

  beforeEach(inject(function($compile, _$rootScope_, _i18n_, iframeElement) {
    $rootScope = _$rootScope_;
    scope = $rootScope.$new();
    i18n = _i18n_;

     iframe = {
      location: {
        src: '/a/path/name'
      },
      angular: {
        element: function() {
          return {
            injector: function() {
              return {
                get: function() {
                  return iframeCatalog;
                }
              };
            }
          };
        }
      },
      document:{
      }
    };
    spyOn(i18n, 'setCookiePath');
    spyOn(iframeElement, 'get').and.returnValue(iframe);

    element = $compile('<iframe preview-localized-iframe></iframe>')(scope);
  }));

  it('should refresh i18n while it has been loaded', function() {
    spyOn(i18n, 'refresh');
    iframeCatalog = {
      strings: {
        'fr-FR': ['frkeys'], 'es-ES': ['eskeys']
      },
      getCurrentLanguage: function() {
        return 'fr-FR';
      }
    };


    element.trigger('load');

    expect(i18n.refresh).toHaveBeenCalledWith(['fr-FR', 'es-ES'], 'fr-FR');
  });

  it('should set i18n cookie path from iframe location pathname', function() {
    expect(i18n.setCookiePath).toHaveBeenCalledWith('/a/path/name');
  });

  it('should unregister event on element when scope is destroyed', function() {
    spyOn(i18n, 'refresh');

    scope.$destroy();
    element.trigger('load');

    expect(i18n.refresh).not.toHaveBeenCalled();
  });
});
