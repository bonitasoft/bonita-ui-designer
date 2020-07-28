describe('datatable widget', function () {

  it('should display a table filled with data', function () {
    browser.get('/bonita/preview/page/no-app-selected/datatable/');

    var table = element(by.css('table.table'));

    var lines = table.all(by.css('tbody tr'));
    expectLineToBe(lines.get(0), '1', 'id labore ex et quam laborum',
      'laudantium enim quasi est quidem magnam voluptate ipsam eos tempora quo necessitatibus dolor quam autem quasi ' +
      'reiciendis et nam sapiente accusantium'
    );
    expectLineToBe(lines.get(1), '1', 'quo vero reiciendis velit similique earum',
      'est natus enim nihil est dolore omnis voluptatem numquam et omnis occaecati quod ullam at voluptatem error ' +
      'expedita pariatur nihil sint nostrum voluptatem reiciendis et'
    );
    expectLineToBe(lines.get(2), '1', 'odio adipisci rerum aut animi',
      'quia molestiae reprehenderit quasi aspernatur aut expedita occaecati aliquam eveniet laudantium omnis quibusdam ' +
      'delectus saepe quia accusamus maiores nam est cum et ducimus et vero voluptates excepturi deleniti ratione'
    );
  });

  function expectLineToBe(line, id, name, body) {
    var tds = line.all(by.tagName('td'));
    expect(tds.get(0).getText()).toEqual(id);
    expect(tds.get(1).getText()).toEqual(name);
    expect(tds.get(2).getText()).toEqual(body);
  }
});
