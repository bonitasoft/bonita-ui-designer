describe('date picker', function () {

  it('should display dates not taking timezone in account', function () {
    browser.get('/designer/preview/page/datePicker/');

    $('input[name="pbDatepicker0"]').click();
    element(by.cssContainingText('.dropdown-menu table td button', '21')).click();
    expect($('pb-text').getText()).toMatch(/"\d\d\d\d-\d\d-21T00:00:00.000Z"/);
    expect($('input[name="pbDatepicker1"]').getAttribute('value')).toMatch(/\d\d\d\d\/\d\d\/21/);

    var value = $('input[name="pbDatepicker2"]').getAttribute('value');
    expect(value).toBe('11/05/2015');

    value = $('input[name="pbDatepicker3"]').getAttribute('value');
    expect(value).toBe('09/30/2015');

  });
});
