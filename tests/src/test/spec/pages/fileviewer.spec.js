describe('fileviewer', function () {

  it('should try to preview document', function () {
    browser.get('/bonita/preview/page/no-app-selected/fileviewer/');

    expect($$('pb-file-viewer a.FileViewer-fileName').count()).toBe(3);
    expect($$('pb-file-viewer a.FileViewer-fileName[box-viewer]').count()).toBe(2);
    //image file
    expect($$('pb-file-viewer img').count()).toBe(1);
    expect($('pb-file-viewer img').getAttribute('src')).toContain('/API/formsDocumentImage?document=2');
    //pdf file
    expect($$('pb-file-viewer iframe').count()).toBe(1);
    expect($('pb-file-viewer iframe').getAttribute('src')).toContain('/API/formsDocumentImage?document=1');
    //unknoqn file
    expect($$('pb-file-viewer span.FileViewer-previewNotAvailable').count()).toBe(1);
    expect($('pb-file-viewer span.FileViewer-previewNotAvailable').getText()).toContain('Preview is not available');
  });
});
