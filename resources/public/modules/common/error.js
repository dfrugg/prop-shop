"use strict";

var errorHandler = angular.module("commonErrorHandler", []);

errorHandler.factory("alertService", ["$timeout", "$rootScope",
    function($timeout, $rootScope){

        var alerts = [],
            setAlertLevel = function(level) {
                //If alerts have errors don"t worry other messages
                if(alerts.filter(function(alert){ return alert.level === "danger"; })) {
                    alerts.level = "danger";
                    return;
                } else{
                    alerts.level = level;
                }
            },
            removeDuplicateAlerts = function(type) {
                //Remove any alerts of the same type
                var existingAlert = alerts.filter(function(alert){ return alert.type === type; });
                if(existingAlert) {
                    alerts.splice(alerts.indexOf(existingAlert), 1);
                }
            },
            broadcastAlertTimeout = function(alert) {
                //After 4 seconds we broadcast that the alert has timed out
                var localAlert = alert;
                $timeout(function(){
                    $rootScope.$broadcast("alert:timeout",localAlert);
                }, 4000);
            };

        return{
            getAlerts: function() {
            	alerts = []; // Separate collection for each calling scope to support persist
                return alerts;
            },
            addAlert: function(level, msg, type, id) {
                var alert = {msg: msg, level: level, type: type, id: id};

                setAlertLevel(level);
                removeDuplicateAlerts(type);

                alerts.push(alert);

                //If the bar is minimized we need to expand it
                if(alerts.minimized) {
                    alerts.minimized = !alerts.minimized;
                }

                broadcastAlertTimeout(alert);
            },
            closeAlert: function(alert) {
                if(alert) {
                    alerts.splice(alerts.indexOf(alert), 1);
                } else {
                    alerts.splice(alerts.length-1, 1);
                }
            },
            closeAlertByContext: function(context) {
                var existingAlert = alerts.filter(function(alert){ return alert.type === context; });
                if(existingAlert) {
                    alerts.splice(alerts.indexOf(existingAlert), 1);
                }
            },
            toggleAlerts: function() {
                alerts.minimized = !alerts.minimized;
            }
        };
    }
]);

/*
 * Error Service provides centralized handling of error responses from the server.
 */
errorHandler.factory("errorService", ["alertService",
    function(alertService){
		var levels = {
				"ERROR": "danger",
				"WARN":"warning",
				"INFO":"info",
                "IMPORTANT": "important"
				};
		var httpErrorHandler = function(error, status, headers, config) {
            //If this is an API call
            if(status === 200 && config.url.indexOf("api") > -1) {
                alertService.closeAlertByContext(config.url);
            }
        	if(error && error.message && error.level){
        		var level = levels[error.level] || "danger";
        		var message = error.message;
        		alertService.addAlert(level, message, config.url, error.trackingId);
        	}
        };
        return{
            handle: httpErrorHandler,
            handleThenDefer: function(deferred){
            	return function(error, status, headers, config){
            		httpErrorHandler(error, status, headers, config);
            		deferred.reject(error);
            	};
            }
        };
    }
]);

/*
 * The Modal Service allows us to easily create bootstrap modals.
 * Use the modal service in your controller as follows:
 *
 * modalService.showModal({}, modalOptions).then(function (result) { Your Logic });
 *
 * You can pass in options or use the defaults found in this service.
 */
errorHandler.factory("modalService", ["$modal",
    function ($modal) {

        var modalDefaults = {
            backdrop: true,
            keyboard: true,
            modalFade: true,
            templateUrl: "view/common/modal.htm",
            windowTemplateUrl: "view/common/modal-window.htm"
        };

        var modalOptions = {
            closeButtonText: "Close",
            actionButtonText: "OK",
            headerText: "Proceed?",
            bodyText: "Perform this action?"
        };

        return {
            showModal: function (customModalDefaults, customModalOptions) {
                if (!customModalDefaults) customModalDefaults = {};
                customModalDefaults.backdrop = "static";
                return this.show(customModalDefaults, customModalOptions);
            },
            show: function (customModalDefaults, customModalOptions) {
                //Create temp objects to work with since we"re in a singleton service
                var tempModalDefaults = {};
                var tempModalOptions = {};

                //Map angular-ui modal custom defaults to modal defaults defined in service
                angular.extend(tempModalDefaults, modalDefaults, customModalDefaults);

                //Map modal.html $scope custom properties to defaults defined in service
                angular.extend(tempModalOptions, modalOptions, customModalOptions);

                if (!tempModalDefaults.controller) {
                    tempModalDefaults.controller = function ($scope, $modalInstance) {
                        $scope.modalOptions = tempModalOptions;
                        $scope.modalOptions.ok = function (result) {
                        	if($scope.modalOptions.requireValue && !(result || $scope.modalOptions.resultValue)){
                        		return;
                        	}
                            if($scope.modalOptions.resultValue) {
                                $modalInstance.close($scope.modalOptions.resultValue);
                            }
                            else {
                                $modalInstance.close(result);
                            }
                        };
                        $scope.modalOptions.close = function (result) {
                            $modalInstance.dismiss("cancel");
                        };
                    };
                }
                return $modal.open(tempModalDefaults).result;
            }
        };
    }
]);
