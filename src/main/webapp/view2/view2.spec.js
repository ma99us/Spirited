'use strict';

describe('myApp.view2 module', function() {

  beforeEach(module('myApp.view2'));
  beforeEach(module('localstorage'));

  describe('view2 controller', function(){

    it('should ....', inject(function($controller, $rootScope) {
      //spec body
      var view2Ctrl = $controller('View2Ctrl', {$scope: $rootScope.$new()});
      expect(view2Ctrl).toBeDefined();
    }));

  });
});