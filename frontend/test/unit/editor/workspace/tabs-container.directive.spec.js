describe('tabsContainer', function() {
  var $compile, $rootScope, element;

  beforeEach(module('ui.router'));
  beforeEach(module('pb.directives', 'pb.common.services', 'pb.factories', 'RecursionHelper'));
  beforeEach(module('pb.directives', 'pb.common.services', 'pb.common.repositories', 'pb.factories', 'RecursionHelper'));
  beforeEach(module('pb.templates', 'gettext'));
  beforeEach(inject(function(_$compile_, _$rootScope_) {
    $compile = _$compile_;
    $rootScope = _$rootScope_;

    // given an element containing the directive
    var template = '<tabs-container tabs-container="tabsContainer" editor="editor"></tabs-container>';
    // when compiling with tabs container containing 2 tabs
    var tabsContainer = {
      tabs: [
        {
          title: 'Tab 1',
          container: {
            rows: [
              []
            ]
          }
        },
        {
          title: 'Tab 2',
          container: {
            rows: [
              []
            ]
          }
        }
      ]
    };
    $rootScope.tabsContainer = tabsContainer;

    $rootScope.editor = {
      isCurrentComponent: function() {
        return false;
      },
      isCurrentTab: function() {
        return false;
      },
      isCurrentRow: function() {
        return false;
      }
    };

    element = $compile(template)($rootScope);
    $rootScope.$digest();
  }));

  it('should display the component', function() {
    // then we should have
    expect(element.find('ul.nav.nav-tabs li').length).toBe(2);
    expect(element.find('ul.nav.nav-tabs li:first').text().trim()).toBe('Tab 1');
    expect(element.find('ul.nav.nav-tabs li:first').hasClass('active')).toBe(true);
    expect(element.find('ul.nav.nav-tabs li:nth(1)').text().trim()).toBe('Tab 2');
    expect(element.find('ul.nav.nav-tabs li:nth(1)').hasClass('active')).toBe(false);
    expect(element.find('container').length).toBe(2);
  });
});
