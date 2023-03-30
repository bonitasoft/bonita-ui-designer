const mobileUserAgent = () => 'Mozilla/5.0 (iPhone; CPU iPhone OS 10_0_1 like Mac OS X) AppleWebKit/602.1.50 ' +
  '(KHTML, like Gecko) Version/10.0 Mobile/14A403 Safari/602.1';

describe('FileViewer Widget', function() {

  var $compile, scope, $window;

  // add both, module required by the widget and locally defined ones
  beforeEach(angular.mock.module('bonitasoft.ui.widgets', 'bonitasoft.ui.directives', 'bonitasoft.ui.filters', 'ui.bootstrap.modal', 'ui.bootstrap.tpls'));

  beforeEach(inject(function(_$compile_, _$window_, $rootScope) {
    $compile = _$compile_;
    $window = _$window_;
    scope = $rootScope.$new();
    scope.properties = {
      'document': {
        url: 'documentDownload?fileName=document.pdf&contentStorageId=someId',
        'id': 'someId',
        fileName: 'document.pdf'
      },
      'type': 'Process document'
    };

    $window.navigator.userAgent = 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.167 Safari/537.36';
  }));


  it('should find extension of given filename', function() {

    let element = $compile('<pb-file-viewer></pb-file-viewer>')(scope);
    scope.$apply();
    let controller = element.controller('pbFileViewer');

    expect(controller.extractFileExtension('test')).toEqual('test');
    expect(controller.extractFileExtension('http://www.test.com/index.html')).toEqual('html');
    expect(controller.extractFileExtension('http://www.test.com/index.html?test&pouetpouet==??##')).toEqual('html');
    expect(controller.extractFileExtension('test.HTml')).toEqual('html');
    expect(controller.extractFileExtension('test.pNG')).toEqual('png');
    expect(controller.extractFileExtension('test.pNG.PDF')).toEqual('pdf');
    expect(controller.extractFileExtension('test.pdf')).toEqual('pdf');
  });

  describe('preview', function() {
    var element;
    beforeEach(function() {
      scope.properties.showPreview = true;

      element = $compile('<pb-file-viewer></pb-file-viewer>')(scope);
      scope.$apply();
    });
    it('should contain frame preview on PDF document', function() {
      expect(element.find('a[box-viewer]').attr('href')).toEqual('../API/formsDocumentImage?document=someId');
      expect(element.find('img').length).toBe(0);
    });

    it('should contain frame preview on PDF archived document', function() {
      scope.properties.document.sourceObjectId = 'sourceId';
      scope.$apply();
      expect(element.find('a[box-viewer]').attr('href')).toEqual('../API/formsDocumentImage?document=sourceId');
      expect(element.find('a[box-viewer]').attr('href')).not.toEqual('../API/formsDocumentImage?document=someId');
      expect(element.find('img').length).toBe(0);
    });

    it('should contain img preview on image URL', function() {
      scope.properties.type = 'URL';
      scope.properties.url = 'http://www.bonitasoft.com/referee.gif';
      scope.$apply();
      expect(element.find('a[box-viewer]').attr('href')).toEqual('http://www.bonitasoft.com/referee.gif');
      expect(element.find('iframe').length).toBe(0);
      expect(element.find('img').length).toBe(1);
    });

    // See https://bonitasoft.atlassian.net/browse/BS-16996
    it('should contain preview for documents initialized by an external system', function() {
      scope.properties.type = 'Process document';
      scope.properties.document = {
        id: 'unused',
        fileName: null,
        url: 'http://westgov.org/images/files/pdf-test.pdf'
      };
      scope.$apply();
      expect(element.find('a[box-viewer]').attr('href')).toEqual('http://westgov.org/images/files/pdf-test.pdf');
      expect(element.find('a[box-viewer]').text().trim()).toEqual('pdf-test.pdf');
    });

    it('should sanitize filename of document', function() {
      scope.properties.type = 'Process document';
      scope.properties.document = {
        id: '321',
        fileName: 'file"with>forbidden.txt',
        url: 'documentDownload?fileName=file"with>forbidden.txt&contentStorageId=321'
      };
      scope.$apply();
      expect(element.find('a.FileViewer-fileName').text().trim()).toEqual('file&#34;with&gt;forbidden.txt');
    });

    it('should download zip process documents initialized by an external system', function() {
      scope.properties.type = 'Process document';
      scope.properties.document = {
        id: 'unused',
        fileName: null,
        url: 'http://westgov.org/zip/files/zip-test.zip'
      };
      scope.$apply();

      expect(element.find('a[box-viewer]').length).toBe(0);
      expect(element.find('a').attr('href')).toEqual('http://westgov.org/zip/files/zip-test.zip');
      expect(element.find('a').attr('title')).toEqual('Download zip-test.zip');
      expect(element.find('a').text().trim()).toEqual('zip-test.zip');
      expect(element.find('span.FileViewer-previewNotAvailable').length).toBe(1);
      expect(element.find('iframe').length).toBe(0);
      expect(element.find('img').length).toBe(0);
    });

    it('should download zip documents with URL type', function() {
      scope.properties.type = 'URL';
      scope.properties.url = 'http://westgov.org/zip/files/zip-test.zip';
      scope.$apply();

      expect(element.find('a[box-viewer]').length).toBe(0);
      expect(element.find('a').attr('href')).toEqual('http://westgov.org/zip/files/zip-test.zip');
      expect(element.find('a').attr('title')).toEqual('Download zip-test.zip');
      expect(element.find('a').text().trim()).toEqual('zip-test.zip');
      expect(element.find('span.FileViewer-previewNotAvailable').length).toBe(1);
      expect(element.find('iframe').length).toBe(0);
      expect(element.find('img').length).toBe(0);
    });

    it('should contain no preview on unsupported document', function() {
      scope.properties.document.fileName = 'bonita.docx';
      scope.properties.document.id = '321';
      scope.properties.document.url = 'documentDownload?fileName=bonita.docx&contentStorageId=321';
      scope.properties.type = 'Process document';
      scope.$apply();
      expect(element.find('a[box-viewer]').length).toBe(0);
      expect(element.find('a').attr('href')).toEqual('../API/documentDownload?fileName=bonita.docx&contentStorageId=321');
      expect(element.find('span.FileViewer-previewNotAvailable').length).toBe(1);
      expect(element.find('iframe').length).toBe(0);
      expect(element.find('img').length).toBe(0);
    });

    it('should contain nothing no document are selected', function() {
      scope.properties.document = {};
      scope.$apply();
      expect(element.find('div').length).toBe(0);
      delete scope.properties.url;
      scope.properties.type = 'URL';
      scope.$apply();
      expect(element.find('div').length).toBe(0);
    });

    it('should open pdf in new tab on click for mobile device', () => {
      spyOn($window, 'open');
      var mobileAgent = mobileUserAgent();
      spyOn($window, 'navigator').and.returnValue({
        userAgent: mobileAgent
      });
      //seems redundant but required in order for the test to work
      $window.navigator.userAgent = mobileAgent;
      element = $compile('<pb-file-viewer></pb-file-viewer>')(scope);
      scope.$apply();

      element.find('embed').click();

      expect($window.open).toHaveBeenCalledWith('../API/formsDocumentImage?document=someId', '_blank');
    });
  });

  describe('fancybox', function() {
    beforeEach(function() {
      scope.properties.type = 'URL';
      scope.properties.url = 'http://www.bonitasoft.com/guide.pdf'
    });

    it('should call fancybox on loading', function() {
      spyOn($.fn, 'fancybox');
      $compile('<pb-file-viewer></pb-file-viewer>')(scope);
      scope.$apply();
      expect($.fn.fancybox).toHaveBeenCalledWith({
        width: '100%',
        height: '100%',
        autoScale: true,
        closeClick: true,
        enableEscapeButton: true,
        errorMsg: 'The requested content cannot be loaded.<br />Please try again later.',
        type: 'iframe',
        title: 'guide.pdf'
      });
      scope.properties.url = 'http://www.bonitasoft.com/referee.gif';
      $.fn.fancybox.calls.reset();
      scope.$apply();
      expect($.fn.fancybox).toHaveBeenCalledWith({
        width: '100%',
        height: '100%',
        autoScale: true,
        closeClick: true,
        enableEscapeButton: true,
        errorMsg: 'The requested content cannot be loaded.<br />Please try again later.',
        type: 'image',
        title: 'referee.gif'
      });
    });

    it('should open the link on new tab for mobile device', () => {
      spyOn($.fn, 'fancybox');
      var mobileAgent = mobileUserAgent();
      spyOn($window, 'navigator').and.returnValue({
        userAgent: mobileAgent
      });
      //seems redundant but required in order for the test to work
      $window.navigator.userAgent = mobileAgent;

      const element = $compile('<pb-file-viewer></pb-file-viewer>')(scope);
      scope.$apply();

      expect(element.find('a[box-viewer]').attr('target')).toEqual('_blank');
      expect($.fn.fancybox).not.toHaveBeenCalled();
    });
  });
});

