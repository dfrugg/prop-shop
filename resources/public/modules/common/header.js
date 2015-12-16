"use strict";

var headerHandler = angular.module("commonHeaderHandler", []);

headerHandler.controller("headerController", ["$rootScope", "$scope", "alertService",
    function($rootScope, $scope, alertService){
      $scope.actions = [{label: "Dashboard", route: "/dashboard"},
                        {label: "-"},
                        {label: "Administration", children: []},
                        {label: "Go To Project", children: [{label: "My Project", templateUrl: "view/admin.html"}]}];
    }
]);


