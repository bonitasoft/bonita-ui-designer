describe('chart', function () {

  it('should draw single and multiple series charts', function () {
    browser.get('/bonita/preview/page/no-app-selected/chart/');

    expect($$('pb-chart div.chart-container canvas').count()).toBe(2);
  });
});
