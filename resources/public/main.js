"use strict";

// Create Module - Add Injectables
var propShopApp = angular.module("propShopApp", [
  "ngRoute",
  "commonHeaderHandler",
  "commonErrorHandler",
  "applicationModule",
  "categoryModule",
  "dashboardModule",
  "environmentModule",
  "propertyModule",
  "tagModule",
  "userModule"
]);

// Configure Routing
propShopApp.config(["$routeProvider", "$httpProvider",
                    function ($routeProvider, $httpProvider) {
                      $routeProvider
                      .when("/", {
                        redirectTo: "/empty"
                      })
                      .when("/empty", {
                        templateUrl: "modules/common/empty.htm"
                      })
                      .when("/application", {
                        templateUrl: "modules/application/application.htm"
                      })
                      .when("/category", {
                        templateUrl: "modules/category/category.htm"
                      })
                      .when("/dashboard", {
                        templateUrl: "modules/dashboard/dashboard.htm"
                      })
                      .when("/environment", {
                        templateUrl: "modules/environment/environment.htm"
                      })
                      .when("/property", {
                        templateUrl: "modules/property/property.htm"
                      })
                      .when("/tag", {
                        templateUrl: "modules/tag/tag.htm"
                      })
                      .when("/user", {
                        templateUrl: "modules/user/user.htm"
                      })
                      .otherwise({
                        redirectTo: "/"
                      });

                      $httpProvider.defaults.headers.common["Cache-Control"] = "no-cache";
                      $httpProvider.defaults.headers.common["X-Requested-With"] = "XMLHttpRequest";
                      $httpProvider.interceptors.push("httpErrorInterceptor");}])
  .factory("httpErrorInterceptor", ["$q", "errorService", function($q, errorService){
    return {
      response: function(response) {
        errorService.handle(response.data, response.status, response.headers, response.config);
        return response;
      },
      responseError: function(response){
        errorService.handle(response.data, response.status, response.headers, response.config);
        return $q.reject(response);
      }
    };
  }]);

// React To Routing Events
propShopApp.run(function ($rootScope, $location, $route, $timeout) {
  $rootScope.config = {};
  $rootScope.config.app_url = $location.url();
  $rootScope.config.app_path = $location.path();
  $rootScope.template = {};
  $rootScope.template.loading = false;
  $rootScope.principal = null;

  $rootScope.$on("$routeChangeStart", function (event, next, current) {
    $timeout(function(){
      $rootScope.template.loading = true;
    });
  });

  $rootScope.$on("$routeChangeSuccess", function () {
    $timeout(function(){
      $rootScope.template.loading = false;
    }, 200);
  });

  $rootScope.$on("$routeChangeError", function () {
    $rootScope.template.loading = false;
  });
});
