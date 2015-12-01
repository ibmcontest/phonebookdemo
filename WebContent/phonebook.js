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

angular.module('phonebook', [])

.controller('PhonebookList', function($scope, $http, $location, $window) {

	var id = 0;
	$scope.entry = {title : "", firstName : "", lastName : "", phoneNumber : ""};

	$http.get('api/phonebook').success(function(data) {
		$scope.entries = data.entries;
	});

	$scope.loadEntry = function() {

		if (id) {
			$http.get('api/phonebook/' + id).success(function(data) {
				$scope.entry = data;
			});
		} else {
			$scope.entry = {title : "", firstName : "", lastName : "", phoneNumber : ""};
		}

	};

	$scope.setId = function(_id) {
		id = _id;
	};

	$scope.remove = function() {

		$http['delete']('api/phonebook/' + id).then(function(data) {
			$window.location.reload();
		});

	};

	$scope.submit = function() {
		if (id) {
			$http.put('api/phonebook/' + id, {
				title : $scope.entry.title,
				firstName : $scope.entry.firstName,
				lastName : $scope.entry.lastName,
				phoneNumber : $scope.entry.phoneNumber
			}).then(function(data) {
				$window.location.reload();
			});
		} else {
			$http.post('api/phonebook', {
				title : $scope.entry.title,
				firstName : $scope.entry.firstName,
				lastName : $scope.entry.lastName,
				phoneNumber : $scope.entry.phoneNumber
			}).then(function(data) {
				$window.location.reload();
			});
		}
	};

});
