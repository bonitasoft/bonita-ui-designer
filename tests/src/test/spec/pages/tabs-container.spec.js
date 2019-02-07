describe('tabsContainer', function () {

  beforeEach(function () {
    browser.get('/bonita/preview/page/no-app-selected/tabsContainer/');
  });
  it('should display 1 tabsContainers with 2 tabs', function () {

    var navTabs = element.all(by.css('.nav-tabs li'));
    expect(navTabs.count()).toBe(2);
    navTabs.first().click();
    expect(element(by.css('.first-tab-content')).isDisplayed()).toBe(true);
    expect(element(by.css('.second-tab-content')).isDisplayed()).toBe(false);

    navTabs.last().click();
    expect(element(by.css('.first-tab-content')).isDisplayed()).toBe(false);
    expect(element(by.css('.second-tab-content')).isDisplayed()).toBe(true);

  });
});
