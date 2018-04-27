var app = angular.module("guiApp", [ "ngRoute", "datatables","datatables.light-columnfilter" ]);
app.config([ '$routeProvider', '$locationProvider', function($routeProvider) {
	$routeProvider

	.when("/", {
		templateUrl : "templates/login.html",
		controller : "listCtrl"
	}).when("/tagpanel", {
		templateUrl : "templates/tagPanel.html",
		controller : "TagCtrl"
	}).when("/templatepanel", {
		templateUrl : "templates/templatePanel.html",
		controller : "TempCtrl"
	}).when("/resultspanel", {
		templateUrl : "templates/resultsPanel.html",
		controller : "PageCtrl"
	}).when("/editPanel", {
		templateUrl : "templates/editPanel.html",
		controller : "EditCtrl"
	}).when("/addPanel", {
		templateUrl : "templates/addPanel.html",
		controller : "AddCtrl"
	}).when("/runpanel", {
		templateUrl : "templates/datatable.html",
		controller : "RunPanelCtrl"
	}).when("/error", {
		templateUrl : "templates/error.html",
		controller : "RunPanelCtrl"
	}).otherwise({
		redirectTo : "/runpanel"
	});
	
} ]);


/* Main Runpanel Controller Here */
app.controller('RunPanelCtrl', function($rootScope, $http, $compile, $scope,DTOptionsBuilder, DTColumnBuilder, $q, $interval, sharedList) {

	$scope.theTime = new Date().toLocaleTimeString();
	$interval(function() {
		$scope.theTime = new Date().toLocaleTimeString();
	}, 1000);

	$http.get("http://localhost:8080/runpanel/failedReasonList").then(
			function(response) {
				$scope.failedReasonList = response.data;
			});

	$http.get("http://localhost:8080/runpanel/failedList").then(
			function(response) {
				$scope.failedList = response.data;
			});

	$http.get("http://localhost:8080/runpanel/passedList").then(
			function(response) {
				$scope.passedList = response.data;
			});
	$http.get("http://localhost:8080/runpanel/labelName").then(
			function(response) {
				$scope.labelName = response.data;

			});

	$http.get("http://localhost:8080/runpanel/failedNum").then(
			function(response) {
				$scope.failedNum = response.data;
			});
	$http.get("http://localhost:8080/runpanel/passedNum").then(
			function(response) {
				$scope.passedNum = response.data;
			});

	$scope.runStat = function() {
		
		$interval(function() {
		
		$http.get("http://localhost:8080/runpanel/labelName").then(
				function(response) {
					$scope.labelName = response.data;

				});
		$http.get("http://localhost:8080/runpanel/failedNum").then(
				function(response) {
					$scope.failedNum = response.data;
				});
		$http.get("http://localhost:8080/runpanel/passedNum").then(
				function(response) {
					$scope.passedNum = response.data;
				});
		
		$http.get("http://localhost:8080/runpanel/runningStatus").then(

		function(response) {
			$scope.runningStatus = response.data;

			if ($scope.runningStatus == "Testcases are running") {

				$('runButton').prop('disabled', true);
				
			}
			if ($scope.runningStatus == "Testcases have finished running") {

				$('runButton').prop('disabled', false);
				
			}
			console.log($scope.runningStatus);
		});
		}, 1000);
	}

	var vm = this;

	$scope.iDmessage = '';
	vm.someClickHandler = someClickHandler;
	$scope.stopMessage = "";


	/* Datatable js here */
	$scope.dtOptions = DTOptionsBuilder.fromFnPromise(
			function() {
				var defer = $q.defer();
				$http.get('http://localhost:8080/runpanel/testcases').then(
						function(result) {
							defer.resolve(result.data);
						});
				return defer.promise;
			}).withOption('createdRow', function(row, data, dataIndex) {
		// Recompiling so we can bind Angular directive to the DT
		$compile(angular.element(row).contents())($scope);
	}).withOption('headerCallback', function(header) {
		if (!vm.headerCompiled) {
			// Use this headerCompiled field to only compile header once
			vm.headerCompiled = true;
			$compile(angular.element(header).contents())($scope);
		}
	}).withOption('rowCallback', rowCallback)
			.withPaginationType('full_numbers').withLightColumnFilter({
				'0' : {
					type : 'text'
				},
				'1' : {
					type : 'text'
				},
				'2' : {
					type : 'text'
				},
				'3' : {
					type : 'select',
					values : [ {
						value : 'CROSS',
						label : 'CROSS'
					}, {
						value : 'NEW',
						label : 'NEW'
					}, {
						value : 'MOD',
						label : 'MOD'
					}, {
						value : 'TO_FIX',
						label : 'TO_FIX'
					}, {
						value : 'PendingReview',
						label : 'Pending Review'
					}, {
						value : 'Total Touch Regression Pack',
						label : 'TOTAL TOUCH REG PCK'
					}, {
						value : 'CXL',
						label : 'CXL'
					} ]
				}

			});

	$scope.dtColumns = [ DTColumnBuilder.newColumn('testID').withTitle('ID'),
			DTColumnBuilder.newColumn('name').withTitle('Name'),
			DTColumnBuilder.newColumn('description').withTitle('Description'),
			DTColumnBuilder.newColumn('category').withTitle('Category') ];

	function someClickHandler(info) {
		
		$scope.iDmessage = info.testID;

		console.log('Double clicked on this testcase id: ' + $scope.iDmessage);

	}

	function rowCallback(nRow, aData, iDisplayIndex, iDisplayIndexFull) {
		// Unbind first in order to avoid any duplicate handler 
		$('td', nRow).unbind('dblclick');
		$('td', nRow).bind('dblclick', function() {
			$scope.$apply(function() {
				vm.someClickHandler(aData);
				$('#addModal').modal('show');
			});
		});
		return nRow;
	}



	$scope.stopTestcase = function() {
		console.log("Clicking stop button to attempt to break from for loop");
		$http.get("http://localhost:8080/runpanel/stopTestcases").then(
				function(response) {
					$scope.message = response.data;
					document.getElementById("emailButton").disabled = false;
					alert($scope.message);

				});
	}


	$http.get("http://localhost:8080/runpanel/labels").then(function(response) {
		$scope.labels = response.data;

	});

	

});



/**
 * Original Datatable configuration with JSON file as datasource
 * DTOptionsBuilder.fromSource('myJS/runpaneltestcases.json')
 */
app.controller('PageCtrl', function($rootScope, DTOptionsBuilder,
		DTColumnBuilder) {

	var vm = this;

	vm.dtOptions = DTOptionsBuilder.fromSource('myJS/runpaneltestcases.json')
			.withPaginationType('full_numbers').withDataProp('data')
			.withLightColumnFilter({
				'0' : {
					type : 'text'
				},
				'1' : {
					type : 'select',
					values : [ {
						value : 'CROSS',
						label : 'CROSS'
					}, {
						value : 'NEW',
						label : 'NEW'
					}, {
						value : 'MOD',
						label : 'MOD'
					}, {
						value : 'TO_FIX',
						label : 'TO_FIX'
					}, {
						value : 'PendingReview',
						label : 'Pending Review'
					}, {
						value : 'Total Touch Regression Pack',
						label : 'TOTAL TOUCH REG PCK'
					}, {
						value : 'CXL',
						label : 'CXL'
					} ]
				},
				'2' : {
					type : 'text'
				},
				'3' : {
					type : 'text'
				},
				'4' : {
					type : 'text'
				},
				'5' : {
					type : 'select',
					values : [ {
						value : "",
						label : ""
					} ]
				},
				'6' : {
					type : 'text'
				},
				'7' : {
					type : 'text'
				},
				'8' : {
					type : 'text'
				},
				'9' : {
					type : 'text'
				}
			});

	vm.dtColumns = [ DTColumnBuilder.newColumn('0').withTitle('Region'),
			DTColumnBuilder.newColumn('1').withTitle('Category'),
			DTColumnBuilder.newColumn('2').withTitle('Test ID'),
			DTColumnBuilder.newColumn('3').withTitle('Name'),
			DTColumnBuilder.newColumn('4').withTitle('Description'),
			DTColumnBuilder.newColumn('5').withTitle('ReleaseNumber'),
			DTColumnBuilder.newColumn('6').withTitle('Enable'),
			DTColumnBuilder.newColumn('7').withTitle('Repeat'),
			DTColumnBuilder.newColumn('8').withTitle('Pass'),
			DTColumnBuilder.newColumn('9').withTitle('Fail')

	];

	console.log("Creating datatable and loading with JSON data");

});