/**
* Copyright 2015 IBM Corp.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

(function () {
  'use strict';

  describe('phonebook', function () {

    var scope, createCtrl, $httpBackend, index;
    var PHONEBOOK_API = 'api/phonebook';

    beforeEach(function () {
      module('phonebook');



      inject(function ($rootScope, $controller, _$httpBackend_, $window) {

        scope = $rootScope.$new();

        $httpBackend = _$httpBackend_;
        window = $window;
        spyOn(window.location, 'reload');


        $httpBackend.when('GET', PHONEBOOK_API)
        .respond({"entries": [ {
          "id":1,
          "title":"X",
          "firstName":"X",
          "lastName":"X",
          "phoneNumber":"X"
        }]});

        $httpBackend.when('GET', PHONEBOOK_API+"/1")
        .respond({
          "id":1,
          "title":"X",
          "firstName":"X",
          "lastName":"X",
          "phoneNumber":"X"
        });

        $httpBackend.when('DELETE', PHONEBOOK_API+"/1")
        .respond(204, "bah");

        $httpBackend.when('PUT', PHONEBOOK_API+"/1", {
          title : "",
          firstName : "",
          lastName : "",
          phoneNumber : "",
        })
        .respond(204, "bah");

        $httpBackend.when('POST', PHONEBOOK_API, {
          title : "",
          firstName : "",
          lastName : "",
          phoneNumber : "",
        })
        .respond(201, "bah");




        createCtrl = function () {
          // The controller always start with a GET all
          $httpBackend.expectGET(PHONEBOOK_API);
          return $controller('PhonebookList', {$scope: scope, $window: window});
        };
      });
    });

    afterEach(function () {
      // $httpBackend.flush();
      $httpBackend.verifyNoOutstandingExpectation();
      $httpBackend.verifyNoOutstandingRequest();
    });

    it('should load the entry list', function () {
      var ctrl = createCtrl();
      $httpBackend.flush();
      expect(scope.entries.length).toBe(1);
    });

    it('should load an entry', function() {
      var ctrl = createCtrl();
      $httpBackend.expectGET(PHONEBOOK_API+"/1");
      scope.setId(1);
      scope.loadEntry();
      $httpBackend.flush();
      expect(scope.entry.title).toBe("X");
    });

    it('should load a blank entry if ID is not set', function() {
      var ctrl = createCtrl();
      scope.loadEntry();
      $httpBackend.flush();
      expect(scope.entry.title).toBe("");
    });

    it('should remove an entry', function() {
      var ctrl = createCtrl();
      $httpBackend.expect('DELETE', PHONEBOOK_API+"/1");
      scope.setId(1);
      scope.remove();
      $httpBackend.flush();
    });

    it('should edit entry with submit if ID is set', function() {
      var ctrl = createCtrl();
      $httpBackend.expect('PUT', PHONEBOOK_API+"/1", {
        title : "",
        firstName : "",
        lastName : "",
        phoneNumber : "",
      });
      scope.setId(1);
      scope.submit();
      $httpBackend.flush();
    });

    it('should create entry with submit if ID is not set', function() {
      var ctrl = createCtrl();
      $httpBackend.expect('POST', PHONEBOOK_API, {
        title : "",
        firstName : "",
        lastName : "",
        phoneNumber : "",
      });
      scope.submit();
      $httpBackend.flush();
    });

  });

})();
