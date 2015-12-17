describe('checkbox', function () {

  it('should display checkbox that return a boolean value', function () {
    browser.get('/designer/preview/page/checkbox/');

    var checkboxes = $$('pb-checkbox');
    expect(checkboxes.count()).toEqual(3); // one is hidden

    // first value should be false
    expect($('pb-text p').getText()).toEqual("false");

    // last checkbox should be disabled and checked by default
    expect(checkboxes.get(2).$('input').isSelected()).toBeTruthy();
    expect(checkboxes.get(2).$('input').isEnabled()).toBeFalsy();

    // click first, value is true and second is checked
    checkboxes.get(0).$('input').click();
    expect(checkboxes.get(1).$('input').isSelected()).toBeTruthy();
    expect($('pb-text p').getText()).toEqual("true");
  });
});
