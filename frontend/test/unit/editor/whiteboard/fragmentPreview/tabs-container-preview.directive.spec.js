describe('tabs container preview', function() {
  var $compile, $rootScope, element;

  beforeEach(angular.mock.module('bonitasoft.designer.editor.whiteboard'));

  beforeEach(inject(function(_$compile_, _$rootScope_) {
    $compile = _$compile_;
    $rootScope = _$rootScope_;

    // given an element containing the directive
    $rootScope.preview = {
      columnClasses: function() {
        return 'col-xs-12';
      }
    };
    var template = '<tabs-container-preview tabs-container="tabsContainer" preview="preview" id="preview"></tabs-container-preview>';
    // when compiling with an input
    $rootScope.tabsContainer = {
      $$id: 'tabs-container-0',
      type: 'tabsContainer',
      dimension: {
        xs: 12
      },
      tabList: [
      ]
    };

    for (var i = 0; i < 2; i++) {
      $rootScope.tabsContainer.tabList.push({
        id: 'pbTabContainer',
        type: 'tabContainer',
        propertyValues : {
          'hidden' : {
            'type' : 'constant',
            'value' : false
          },
          'cssClasses' : {
            'type' : 'constant',
            'value' : ''
          },
          'disabled' : {
            'type' : 'constant',
            'value' : false
          },
          'title' : {
            'type' : 'interpolation',
            'value' : 'Tab ' + (i + 1)
          }
        },
        $$parentTabsContainer: $rootScope.tabsContainer,
        container: {
          rows: [[]]
        }
      });
    }

    element = $compile(template)($rootScope);
    $rootScope.$digest();
  }));

  it('should display the tabs container in preview', function() {
    // then we should have the container as one row
    expect(element.find('.nav.nav-tabs').length).toBe(1);
    expect(element.find('li a').length).toBe(2);
    expect(element.find('.tab-body').length).toBe(2);
  });

  it('should switch between tabs', function() {
    expect(element.find('li:first').hasClass('active')).toBe(true);
    expect(element.find('.tab-body:first').hasClass('ng-hide')).toBe(false);
    expect(element.find('li:nth(1)').hasClass('active')).toBe(false);
    expect(element.find('.tab-body:nth(1)').hasClass('ng-hide')).toBe(true);

    element.find('li:nth(1) a').click();

    expect(element.find('li:first').hasClass('active')).toBe(false);
    expect(element.find('.tab-body:first').hasClass('ng-hide')).toBe(true);
    expect(element.find('li:nth(1)').hasClass('active')).toBe(true);
    expect(element.find('.tab-body:nth(1)').hasClass('ng-hide')).toBe(false);
  });
});
