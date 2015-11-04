describe('arrays', function() {
  var arrays;

  beforeEach(angular.mock.module('bonitasoft.designer.common.services'));
  beforeEach(inject(function(_arrays_) {
    arrays = _arrays_;
  }));

  describe('Move an item to the left', function() {

    it('should move an item on its left if the array is full', function() {

      var test = ['test-1', 'test-2', 'test-3'];
      arrays.moveLeft('test-2', test);
      expect(test[0]).toBe('test-2');
      expect(test[1]).toBe('test-1');
      expect(test[2]).toBe('test-3');

    });

    it('should move an item on its left if the array length = 2', function() {

      var test = ['test-1', 'test-2'];
      arrays.moveLeft('test-2', test);
      expect(test[0]).toBe('test-2');
      expect(test[1]).toBe('test-1');

    });

    it('should not change the array if its length = 1', function() {

      var test = ['test-1'];
      arrays.moveLeft('test-1', test);
      expect(test[0]).toBe('test-1');
      expect(test[1]).toBeUndefined();

    });

    it('should add the item if the item does not exist', function() {

      var test = ['test-1'];
      arrays.moveLeft('test-2', test);
      expect(test[0]).toBe('test-2');
      expect(test[1]).toBe('test-1');
    });

    it('should add the item if the item does not exist and length is empty', function() {

      var test = [];
      arrays.moveLeft('test-2', test);
      expect(test[0]).toBe('test-2');
      expect(test[1]).toBeUndefined();
    });

  });

  describe('Move an item to the right', function() {

    it('should move an item on its left if the array is full', function() {

      var test = ['test-1', 'test-2', 'test-3'];
      arrays.moveRight('test-2', test);
      expect(test[0]).toBe('test-1');
      expect(test[1]).toBe('test-3');
      expect(test[2]).toBe('test-2');

    });

    it('should move an item on its left if the array is length = 2', function() {

      var test = ['test-1', 'test-2'];
      arrays.moveRight('test-1', test);
      expect(test[0]).toBe('test-2');
      expect(test[1]).toBe('test-1');

    });

    it('should not change the array if its length = 1', function() {

      var test = ['test-1'];
      arrays.moveRight('test-1', test);
      expect(test[0]).toBe('test-1');
      expect(test[1]).toBeUndefined();

    });

    it('should add the item if the item does not exist', function() {

      var test = ['test-1'];
      arrays.moveRight('test-2', test);
      expect(test[0]).toBe('test-1');
      expect(test[1]).toBe('test-2');
    });

    it('should add the item if the item does not exist and length is empty', function() {

      var test = [];
      arrays.moveRight('test-2', test);
      expect(test[0]).toBe('test-2');
      expect(test[1]).toBeUndefined();
    });

  });

  describe('Move an item to a custom position', function() {

    it('should fill an array if this one is empty', function() {
      var test = [];
      arrays.moveAtPosition('test-1',0,test);
      expect(test[0]).toBe('test-1');
      expect(test.length).toBe(1);

      test.length = 0;

      arrays.moveAtPosition('test-1',3,test);
      expect(test[0]).toBe('test-1');
      expect(test.length).toBe(1);
    });

    it('should move to a custom position', function() {
      var test = ['test-1', 'test-2'];
      arrays.moveAtPosition('test-1',1,test);
      expect(test[0]).toBe('test-2');
      expect(test[1]).toBe('test-1');
      expect(test.length).toBe(2);

      test = ['test-1', 'test-2','test-3'];
      arrays.moveAtPosition('test-1',2,test);
      expect(test[0]).toBe('test-2');
      expect(test[1]).toBe('test-3');
      expect(test[2]).toBe('test-1');
      expect(test.length).toBe(3);
    });

  });

  it('should remove one element in an array of Objects', function() {
    var array = [
      { code: 'c1', label: 'l1' },
      { code: 'c2', label: 'l2' },
      { code: 'c3', label: 'l3' }];

    arrays.remove({ code: 'c2', label: 'l2' }, array, function(elt1, elt2) {
      if ((!elt1 && elt2) || (elt1 && !elt2)) {
        return false;
      }
      return elt1.code === elt2.code;
    });

    expect(array.length).toBe(2);
    expect(array).toEqual([
      { code: 'c1', label: 'l1' },
      { code: 'c3', label: 'l3' }]);
  });

  it('should not remove element in an empty array', function() {
    var array = [];
    arrays.remove({ code: 'c2', label: 'l2' }, array);
    expect(array.length).toBe(0);
  });

  it('should not remove an empty element in an array of Objects', function() {
    var array = [
      { code: 'c1', label: 'l1' },
      { code: 'c2', label: 'l2' },
      { code: 'c3', label: 'l3' }];

    arrays.remove({}, array, function(elt1, elt2) {
      if ((!elt1 && elt2) || (elt1 && !elt2)) {
        return false;
      }
      return elt1.code === elt2.code;
    });

    expect(array.length).toBe(3);
  });

  it('should not remove an element when equalityTester not defined', function() {
    var array = [
      { code: 'c1', label: 'l1' },
      { code: 'c2', label: 'l2' },
      { code: 'c3', label: 'l3' }];

    arrays.remove({ code: 'c2', label: 'l2' }, array);

    expect(array.length).toBe(3);
  });

  it('should remove two elements in an array of integer', function() {
    var array = [2, 1, 6, 1];
    arrays.remove(1, array);
    expect(array.length).toBe(2);
    expect(array).toEqual([2, 6]);
  });

  it('should remove first element of an array', function() {
    var array = [1, 1, 2, 3];

    array = arrays.removeFirst(1, array);

    expect(array).toEqual([1, 2, 3]);
  });

  it('should do nothing when removing first element of an array that is not in array', function() {
    var array = [1, 1, 2, 3];

    array = arrays.removeFirst(4, array);

    expect(array).toEqual([1, 1, 2, 3]);
  });

  it('should insert an element in an array of integer at correct position', function() {
    var array = [2, 1, 6, 1];
    arrays.insertAtPosition(3, 1, array);
    expect(array.length).toBe(5);
    expect(array).toEqual([2, 3, 1, 6, 1]);
  });

  it('should insert an element in an array of integer at the end if position is incorrect', function() {
    var array = [2, 1, 6, 1];
    arrays.insertAtPosition(3, -2, array);
    expect(array.length).toBe(5);
    expect(array).toEqual([2, 1, 6, 1, 3]);
  });

  it('should insert an element in an array of integer at the end if position is not provided', function() {
    var array = [2, 1, 6, 1];
    arrays.insertAtPosition(3, undefined, array);
    expect(array.length).toBe(5);
    expect(array).toEqual([2, 1, 6, 1, 3]);
  });
});
