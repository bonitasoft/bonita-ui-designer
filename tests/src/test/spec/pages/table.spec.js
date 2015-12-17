describe('table', function () {

  function expectLineToBe(line, id, name) {
    var tds = line.all(by.tagName('td'));
    expect(tds.get(0).getText()).toEqual(id);
    expect(tds.get(1).getText()).toEqual(name);
  }

  beforeEach(function () {
    browser.get('/designer/preview/page/table/');
  });

  it('should display a table filled with data that can be selected', function () {
    var table = element(by.css('table.table'));

    // table should be clickable
    expect(table.getAttribute('class')).toContain('table-hover');

    // should contain repositories list
    var lines = table.all(by.css('tbody tr'));
    expectLineToBe(lines.get(0), '11336005', 'bonita-connectors');
    expectLineToBe(lines.get(1), '11336028', 'bonita-connectors-assembly');
    expectLineToBe(lines.get(2), '12897159', 'bonita-connectors-packaging');
    expectLineToBe(lines.get(3), '24060487', 'bonita-custom-page-seed');
    expectLineToBe(lines.get(4), '11337159', 'bonita-distrib');

    // should select a line
    lines.get(0).click();
    expect(element(by.css('pb-text p')).getText()).toEqual('bonitasoft/bonita-connectors');
  });

});
