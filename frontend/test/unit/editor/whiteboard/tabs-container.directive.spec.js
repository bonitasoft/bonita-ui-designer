import aTab from '../../utils/builders/TabElementBuilder';

describe('tabsContainer', function() {
  var $compile, $rootScope, element;

  beforeEach(angular.mock.module('bonitasoft.designer.editor.whiteboard'));

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
      isCurrentRow: function() {
        return false;
      },
      selectComponent: function() {
        return true;
      }
    };

    element = $compile(template)($rootScope);
    $rootScope.$digest();
  }));

  it('should display the component', function() {
    // then we should have 2 tabs + one 'plus' button
    expect(element.find('ul.nav.nav-tabs li').length).toBe(3);
    expect(element.find('ul.nav.nav-tabs li:first').text().trim()).toBe('Tab 1');
    expect(element.find('ul.nav.nav-tabs li:first').hasClass('active')).toBe(true);
    expect(element.find('ul.nav.nav-tabs li:nth(1)').text().trim()).toBe('Tab 2');
    expect(element.find('ul.nav.nav-tabs li:nth(1)').hasClass('active')).toBe(false);
    expect(element.find('container').length).toBe(2);
  });

  it('should add a new tab', function() {
    $rootScope.tabsContainer = {
      tabs: [
        aTab().title('tab-1'),
        aTab().title('tab-2')
      ]
    };
    $rootScope.$apply();

    element.find('ul.nav.nav-tabs li:nth(2) a').triggerHandler({ type: 'click' });

    expect($rootScope.tabsContainer.tabs.length).toBe(3);
  });

  it('should remove a tab', function() {
    $rootScope.tabsContainer = {
      tabs: [
        aTab().title('tab-1'),
        aTab().title('tab-2')
      ]
    };
    spyOn($rootScope.editor, 'isCurrentComponent').and.returnValue(true);
    $rootScope.$apply();

    element.find('button.btn-tab.fa-times-circle').first().triggerHandler({ type: 'click' });

    expect($rootScope.tabsContainer.tabs.length).toBe(1);
  });

  it('should show remove button when there is more than one tab', function() {
    $rootScope.tabsContainer = {
      tabs: [
        aTab().title('tab-1'),
        aTab().title('tab-2')
      ]
    };
    spyOn($rootScope.editor, 'isCurrentComponent').and.returnValue(true);
    $rootScope.$apply();

    expect(element.find('button.btn-tab.fa-times-circle').length).toBe(2); // two tabs
  });

  it('should hide remove button when there is only one tab', function() {
    $rootScope.tabsContainer = {
      tabs: [
        aTab().title('tab-1'),
      ]
    };
    spyOn($rootScope.editor, 'isCurrentComponent').and.returnValue(true);
    $rootScope.$apply();

    expect(element.find('button.btn-tab.fa-times-circle').length).toBe(0);
  });
});
