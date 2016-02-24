"use strict";

var applicationModule = angular.module("applicationModule", []);

applicationModule.controller("applicationController", ["$rootScope", "$scope", "$http",
  function($rootScope, $scope, $http){
    $scope.applications = [];
    $scope.name = '';
    $scope.orgUuid = '';

    $scope.loadApplications = function() {
      $http({url: "/sapi/app/", method: "GET"})
        .success(function(data) {
          $scope.applications = data;
        })
        .error(function(data, status, headers, config){
          console.log("ERROR");
        });
    };

    $scope.update = function(idx) {
      var application = $scope.applications[idx];

      var res = $http.put('/sapi/app/', application);
      res.success(function(data, status, headers, config) {
        $scope.applications[idx] = data;
      });
      res.error(function(data, status, headers, config) {
        alert( "Error: " + JSON.stringify({data: data}));
      });
    };

    $scope.create = function() {
      var application = { name: $scope.name };
      application['organization-uuid'] = $scope.orgUuid;

      var res = $http.post('/sapi/app/', application);
      res.success(function(data, status, headers, config) {
        $scope.applications.push(data);
      });
      res.error(function(data, status, headers, config) {
        alert( "Error: " + JSON.stringify({data: data}));
      });

      $scope.name = '';
      $scope.orgUuid = '';
    };

    $scope.deactivate = function (idx) {
      var application = $scope.applications[idx];

      var res = $http.delete('/sapi/app/' + application.uuid);
      res.success(function(data, status, headers, config) {
        $scope.applications[idx] = data;
      });
      res.error(function(data, status, headers, config) {
        alert( "Error: " + JSON.stringify({data: data}));
      });
    };

    $scope.loadApplications();
  }
]);
