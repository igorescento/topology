var headerModule = angular.module('header',['ngRoute']);

/* button to link mappings */
var mappings = {
  "button-connect": "connect",
  "button-topology": "topology",
  "button-lsa": "lsa",
  "button-router": "routers",
  "button-network": "networks"
};

/* live time controller */
headerModule.controller('TimeCtrl', function($scope, $interval) {
  var tick = function() {
    $scope.clock = Date.now();
  };

  tick();
  $interval(tick, 1000);
});

/* button click controller to display html */
headerModule.controller('ButtonClick', function ($scope, $location) {
  $scope.ButtonClick = function (value) {
    var found = false;

    for(var key in mappings){
      if(value.currentTarget.className === key){
        found = true;
        $location.url(mappings[key]);
      }
    }

    if(!found){
      $location.url("connect");
    }
  }
});
