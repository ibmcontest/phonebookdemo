angular.module('phonebook', [])

.controller('PhonebookList', function($scope, $http, $location) {

	console.log("list");
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

		console.log("setid");
		id = _id;
	};

	$scope.remove = function() {

		console.log("remove");
		$http['delete']('api/phonebook/' + id).then(function(data) {
			location.reload();
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
				location.reload();
			});
		} else {
			$http.post('api/phonebook', {
				title : $scope.entry.title,
				firstName : $scope.entry.firstName,
				lastName : $scope.entry.lastName,
				phoneNumber : $scope.entry.phoneNumber
			}).then(function(data) {
				location.reload();
			});
		}
	};

});
