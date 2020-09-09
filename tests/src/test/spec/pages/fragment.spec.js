describe('fragment', function () {

  it('should be allow binding data to exposed fragment data', function () {
    browser.get('/bonita/preview/page/no-app-selected/pageWithFragment/');

    $('pb-fragment-person input').sendKeys(' & colin');

    expect($('pb-text p').getText()).toEqual('Hello vincent & colin');
  });
});
