var Preview = require('../pages/preview.page.js');

describe('preview test', function() {

  it('should preview a page and render an iframe that scales with resolutions', function() {
    var preview = Preview.getPage('personPage');

    // then we should have an iframe with a desktop width
    expect(preview.iframeWidth).toBe('992');
    expect(preview.iframeSrc).toContain('preview/page/no-app-selected/personPage');

    $('i.fa-mobile').click();
    expect(preview.iframeWidth).toBe('320');

  });

  it('should preview a fragment and render an iframe', function() {
    var preview = Preview.getFragment('personFragment');

    expect(preview.iframeSrc).toContain('preview/fragment/no-app-selected/personFragment');
  });

  it('should preview a page and render an iframe that scales with resolutions', function() {
    var preview = Preview.getFragment('personFragment');

    // then we should have an iframe with a desktop width
    expect(preview.iframeWidth).toBe('992');
    expect(preview.iframeSrc).toContain('preview/fragment/no-app-selected/personFragment');

    // we should have the correct size for xs
    $('i.fa-mobile').click();
    expect(preview.iframeWidth).toBe('320');
  });
});
