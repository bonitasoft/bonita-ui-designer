describe('date picker', function () {

  it('should display dates not taking timezone in account', function () {
    browser.get('/bonita/preview/page/no-app-selected/datePicker/');

    $('input[name="pbDatepicker0"]').click();
    element(by.cssContainingText('.dropdown-menu table td button', '21')).click();
    expect($('pb-text').getText()).toMatch(/"\d\d\d\d-\d\d-21T00:00:00.000Z"/);
    expect($('input[name="pbDatepicker1"]').getAttribute('value')).toMatch(/\d\d\d\d\/\d\d\/21/);

    var value = $('input[name="pbDatepicker2"]').getAttribute('value');
    expect(value).toBe('11/05/2015');

    value = $('input[name="pbDatepicker3"]').getAttribute('value');
    expect(value).toBe('09/30/2015');
  });

  it('should show error date message on wrong date', function () {
    browser.get('/bonita/preview/page/no-app-selected/datePicker/');

    $('input[name="pbDatepicker0"]').click();
    element(by.cssContainingText('.dropdown-menu table td button', '21')).click();

    $('input[name="pbDatepicker2"]').sendKeys('bonita');

    expect($('.text-danger').getText()).toEqual('This is not a valid date');
  });
});
