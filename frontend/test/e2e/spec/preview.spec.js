var Preview = require('../pages/preview.page.js');

describe('preview test', function() {

  it('should preview a page and render an iframe that scales with resolutions', function() {
    var preview = Preview.getPage('person-page');

    // then we should have an iframe with a desktop width
    expect(preview.iframeWidth).toBe('100%');
    expect(preview.iframeSrc).toContain('preview/page/person-page');

  });
});
