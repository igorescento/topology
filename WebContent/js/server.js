var serverModule = angular.module('server',[]);

serverModule.factory('routerData', ['$http', function ($http) {

    var urlBase = 'api/';
    var routerData = {};

    routerData.getLsa = function (data) {
        return $http.post(urlBase + 'mikrotik', data);
    };
    return routerData;
}]);
