describe('date time picker', function () {

  it('should display dates and time not taking timezone in account', function () {
    browser.get('/bonita/preview/page/no-app-selected/dateTimePicker/');

    $('input[name="pbDateTimepicker0date"]').click();
    element(by.cssContainingText('.dropdown-menu table td button', '21')).click();
    expect($('input[name="pbDateTimepicker0date"]').getAttribute('value')).toMatch(/\d\d\/21\/\d\d\d\d/);
    expect($('input[name="pbDateTimepicker0time"]').getAttribute('value')).toMatch("4:17:00 PM");

   });

  it('should show error date message on wrong date', function () {
    browser.get('/bonita/preview/page/no-app-selected/dateTimePicker/');

    $('input[name="pbDateTimepicker0date"]').click();
    $('input[name="pbDateTimepicker0date"]').sendKeys('bonita');

    expect($('.text-danger').getText()).toEqual('This is not a valid date or time');
  });
});
