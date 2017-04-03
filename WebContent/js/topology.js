/*jshint esversion: 6 */

var netModule = angular.module('topology', []);

netModule.controller('netgraph', function($rootScope, $scope, $http, $interval, $timeout, $location, $window) {

    $scope.buttonTitle = "Show Sink Tree";
    $scope.header = "Complete";
    $scope.externalTitle = "Hide External";
    $scope.responseReady = false;
    $scope.topology = true;
    $scope.demoData = null;

    /* load JSON with demo data */
    if($rootScope.isDemo){
        $scope.background = "#ffffff";
        $scope.responseReady = true;

        $http.get('demo/demo_topology.json')
            .then(function(res){
                $scope.demoData = JSON.parse(JSON.stringify(res.data));
                if(d3.select("svg")){
                    d3.select("svg").remove();
                    d3.select(".njg-metadata").remove();
                    d3.select(".njg-overlay").remove();
                }
                processData(res.data, "10.99.0.148");
            });

      //hide some elements that are not available in demo version
      //var sinktree = document.getElementsByClassName('sink-tree');
      var externalB = document.getElementsByClassName('button-external');
      /*for (var i = 0; i < sinktree.length; i += 1) {
          sinktree[i].style.display = 'none';
      }*/

      //populate shortest path selectors
      $http.get('demo/distinct_routers.json')
          .then(function(res){
              $scope.routers = res.data;
          });
      $scope.routerfilter = "10.99.0.148";

      for (var i = 0; i < externalB.length; i += 1) {
          externalB[i].style.display = 'none';
      }

      $scope.SwitchView = function () {
          $scope.buttonTitle = $scope.buttonTitle === "Show Sink Tree" ? "Show Topology" : "Show Sink Tree";
          $scope.header = $scope.header === "Complete" ? "Sink Tree" : "Complete";

          //switch view active - loading topology
          if($scope.buttonTitle === "Show Sink Tree"){
              $scope.topology = true;
              $http.get('demo/demo_topology.json')
                  .then(function(res){
                      if(d3.select("svg")){
                          d3.select("svg").remove();
                          d3.select(".njg-metadata").remove();
                          d3.select(".njg-overlay").remove();
                      }
                      processData(res.data, "10.99.0.148");
                  });
          }

          else {
              $scope.topology = false;
              $http.get('demo/demo_topology_sinktree.json')
                  .then(function(res){
                      if(d3.select("svg")){
                          d3.select("svg").remove();
                          d3.select(".njg-metadata").remove();
                          d3.select(".njg-overlay").remove();
                      }
                      processData(res.data, "10.99.0.148");
                  });

              $scope.changedSource = function(rootNode){
                  if(rootNode !== ''){
                      $scope.routerfilter = rootNode;
                      getDemoTree($scope.routerfilter, $scope.demoData);
                      $scope.destinationFilter = "";
                      $scope.distance = 0;
                  }
              };

              //highlight shortest path when destination selected
              $scope.changedDestination = function(destination){
                  var links;
                  if(destination !== '' && $scope.routerfilter){
                      if(destination === $scope.routerfilter){
                          $window.alert("Same router selected. Please select a different router.");
                          links = d3.selectAll(".njg-link")[0];
                          for(var j = 0; j < links.length; j++) {
                              links[j].style.stroke = "#999";
                          }
                          $scope.distance = 0;
                      }
                      else {
                          //clear each time destination is changed
                          links = d3.selectAll(".njg-link")[0];
                          for(var k = 0; k < links.length; k++) {
                              links[k].style.stroke = "#999";
                          }
                          getDemoPath($scope.routerfilter, destination, $scope.demoData);
                      }
                  }
              };
          }
      };
    }
    /* demo end */

    /* live data */
    else {

        $scope.SwitchView = function () {
            $scope.buttonTitle = $scope.buttonTitle === "Show Sink Tree" ? "Show Topology" : "Show Sink Tree";
            $scope.header = $scope.header === "Complete" ? "Sink Tree" : "Complete";

            //switch view active - loading topology
            if($scope.buttonTitle === "Show Sink Tree"){
                $scope.topology = true;
                getFullNetwork();
                $scope.externalTitle = "Hide External";

                // call get method every n seconds
                $scope.Timer = $interval(function() {
                    $rootScope.connectionDetails.time = new Date().toLocaleString();
                    getFullNetwork();
                    getRouters();
                    $scope.externalTitle = "Hide External";
                }, 30000);
            }

            else {
                $scope.topology = false;
                $interval.cancel($scope.Timer);
                $scope.externalTitle = "Hide External";
                getSinkTree($rootScope.connectionDetails.routerid);

                $scope.changedSource = function(rootNode){
                    if(rootNode !== ''){
                        $scope.routerfilter = rootNode;
                        getSinkTree($scope.routerfilter);
                        $scope.destinationFilter = "";
                        $scope.distance = 0;
                    }
                };

                //highlight shortest path when destination selected
                $scope.changedDestination = function(destination){
                    if(destination !== '' && $scope.routerfilter){
                        var links;
                        if(destination === $scope.routerfilter){
                            $window.alert("Same router selected. Please select a different router.");
                            links = d3.selectAll(".njg-link")[0];
                            for(var k = 0; k < links.length; k++) {
                                links[k].style.stroke = "#999";
                            }
                            $scope.distance = 0;
                        }
                        else {
                            //clear each time destination is changed
                            links = d3.selectAll(".njg-link")[0];
                            for(var l = 0; l < links.length; l++) {
                                links[l].style.stroke = "#999";
                            }
                            getShortestPath($scope.routerfilter, destination);
                        }
                    }
                };
            }
        };

        /**
        same approach as in switch view where data is polled initially every n seconds
        poll data very often after table deletion
        */
        var getDataPromise = $interval(function(){
          if(!$scope.responseReady){
              getRouters();
              if($scope.buttonTitle === "Show Sink Tree"){
                  getFullNetwork();
              }
              else {
                  getSinkTree($rootScope.connectionDetails.routerid);
              }
          }
        }, 1000);

        // call get method every n seconds
        $scope.Timer = $interval(function() {
            getRouters();
            getFullNetwork();
        }, 30000);

        /* Show / Hide external networks - nodes and links included */
        $scope.ShowExternal = function() {
            var boolVal, externals;

            externals = document.getElementsByClassName("external");
            $scope.externalTitle = $scope.externalTitle === "Hide External" ? "Show External" : "Hide External";
            boolVal = $scope.externalTitle === "Hide External" ? true : false;

            showExternals(externals, boolVal);
        };

    }

    /* cancel timer when changing view */
    $scope.$on("$destroy",function(){
        if (angular.isDefined($scope.Timer)) {
            $interval.cancel($scope.Timer);
            $interval.cancel(getDataPromise);
        }
    });

    /* Show external function */
    function showExternals(element, boolVal) {
        if(!boolVal){
            Array.from(element).forEach(v => {
                v.style.display = "none";
            });
        }
        else {
            Array.from(element).forEach(v => {
                v.style.display = "inherit";
            });
        }
    }

    /* Method to get the data from source DB */
    function getFullNetwork(){

        var config = {
            method: 'GET',
            url: 'http://localhost:8080/topology/api/topology/full'
        };

        $http(config)
            .then(function(response) {
                if(response.data.nodes.length !== 0 || response.data.edges.length !== 0){
                    /* remove the svg element and create new graph */
                    if(d3.select("svg")){
                        d3.select("svg").remove();
                        d3.select(".njg-metadata").remove();
                        d3.select(".njg-overlay").remove();
                    }

                    $rootScope.connectionDetails.time = new Date().toLocaleString();
                    processData(response.data, $rootScope.connectionDetails.routerid);
                    $scope.responseReady = true;
                    $scope.background = "#ffffff";

                    var externals = document.getElementsByClassName("external");
                    var boolVal = $scope.externalTitle === "Hide External" ? true : false;
                    showExternals(externals, boolVal);
                }
                else {
                    if(d3.select("svg")){
                        d3.select("svg").remove();
                        d3.select(".njg-metadata").remove();
                        d3.select(".njg-overlay").remove();
                    }
                    $scope.responseReady = false;
                    $scope.background = "#fffff0";
                }
            })
            .catch(function(error){
                $window.alert("Error retrieving full topology. Please try again.");
                $scope.responseReady = false;
                $rootScope.isDataLoaded = false;
                //$location.path('/connect');
            });

    }

    /* Method to get the sink tree */
    function getSinkTree(routerId){

        var config = {
            method: 'GET',
            url: 'http://localhost:8080/topology/api/topology/sinktree/' + routerId
        };

        $http(config)
            .then(function (response) {
              /* remove the svg element and create new graph */
              if(response.data.nodes.length !== 0 || response.data.edges.length !== 0){
                  /* remove the svg element and create new graph */
                  if(d3.select("svg")){
                      d3.select("svg").remove();
                      d3.select(".njg-metadata").remove();
                  }
                  $rootScope.connectionDetails.time = new Date().toLocaleString();
                  processData(response.data, routerId);
                  $scope.responseReady = true;
                  $scope.background = "#ffffff";
              }
              else {
                  if(d3.select("svg")){
                      d3.select("svg").remove();
                      d3.select(".njg-metadata").remove();
                  }
                  $scope.responseReady = false;
                  $scope.background = "#fffff0";
              }
            })
            .catch(function(error){
                $window.alert("Error retrieving sink tree. Please try again.");
            });

    }

    /* Method to get the routers for selector*/
    function getRouters(){
        var config = {
            method: 'GET',
            url: 'http://localhost:8080/topology/api/type/distinctrouter'
        };
        $http(config)
            .then(function (response) {
                $scope.routers = response.data;
                //initialize the selector for source with router where we're logging in
                $scope.routerfilter = $rootScope.connectionDetails.routerid;
            })
            .catch(function(error){
                $scope.responseReady = false;
                $rootScope.isDataLoaded = false;
                $window.alert("Error retrieving routers info. Please try again.");
            });
    }

    /* Method to get the shortest path between two nodes*/
    function getShortestPath(from, to){

        var config = {
            method: 'GET',
            url: 'http://localhost:8080/topology/api/topology/shortestpath/' + from + '/' + to
        };
        $http(config)
            .then(function (response) {
                var resp, routers, distances;

                if(Object.keys(response.data[1]).length){
                  resp = response.data[1];

                  routers = Object.keys(resp).sort(function(a,b){ return resp[a]-resp[b]; });

                  distances = response.data[0];
                  $scope.distance = distances[to];

                  for(var i = 0; i < routers.length - 1; i++){
                      var linkA = routers[i].replace(/\./g, '-') + '_' + routers[i+1].replace(/\./g, '-');
                      var linkB = routers[i+1].replace(/\./g, '-') + '_' + routers[i].replace(/\./g, '-');

                      var el = document.getElementsByClassName(linkA);
                      var el2 = document.getElementsByClassName(linkB);
                      if(el.length > 0) {
                          el[0].style.stroke = '#f90808';
                      }
                      if(el2.length > 0){
                          el2[0].style.stroke = '#f90808';
                      }
                  }
                }
                else {
                  $window.alert("Request unsuccessful due to database update. Please try again.");
                }
            })
            .catch(function(error){
                $window.alert("Error retrieving shortest path data due to possible overlap with DB update. Please try again.");
            });

    }

    //mehod to return DEMO sink tree
    function getDemoTree(routerId, data){

        var config = {
            method: 'POST',
            url: 'http://localhost:8080/topology/api/topology/demotree/' + routerId,
            headers: {'Content-Type': 'application/json'},
            data: data
        };

        $http(config)
            .then(function (response) {
              /* remove the svg element and create new graph */
              if(response.data.nodes.length !== 0 || response.data.edges.length !== 0){
                  /* remove the svg element and create new graph */
                  if(d3.select("svg")){
                      d3.select("svg").remove();
                      d3.select(".njg-metadata").remove();
                  }
                  $rootScope.connectionDetails.time = new Date().toLocaleString();
                  processData(response.data, routerId);
                  $scope.responseReady = true;
                  $scope.background = "#ffffff";
              }
              else {
                  if(d3.select("svg")){
                      d3.select("svg").remove();
                      d3.select(".njg-metadata").remove();
                  }
                  $scope.responseReady = false;
                  $scope.background = "#fffff0";
              }
            })
            .catch(function(error){
                $window.alert("Error retrieving sink tree. Please try again.");
            });

    }

    /* Method to get the DEMO shortest path between two nodes*/
    function getDemoPath(from, to, data){
        var config = {
            method: 'POST',
            url: 'http://localhost:8080/topology/api/topology/demopath/' + from + '/' + to,
            data: data
        };
        $http(config)
            .then(function (response) {
                var resp, routers, distances;

                if(Object.keys(response.data[1]).length){
                  resp = response.data[1];

                  routers = Object.keys(resp).sort(function(a,b){ return resp[a]-resp[b]; });

                  distances = response.data[0];
                  $scope.distance = distances[to];

                  for(var i = 0; i < routers.length - 1; i++){
                      var linkA = routers[i].replace(/\./g, '-') + '_' + routers[i+1].replace(/\./g, '-');
                      var linkB = routers[i+1].replace(/\./g, '-') + '_' + routers[i].replace(/\./g, '-');

                      var el = document.getElementsByClassName(linkA);
                      var el2 = document.getElementsByClassName(linkB);
                      if(el.length > 0) {
                          el[0].style.stroke = '#f90808';
                      }
                      if(el2.length > 0){
                          el2[0].style.stroke = '#f90808';
                      }
                  }
                }
                else {
                  $window.alert("Request unsuccessful due to database update. Please try again.");
                }
            })
            .catch(function(error){
                $window.alert("Error retrieving shortest path data due to possible overlap with DB update. Please try again.");
            });

    }
});

/* Method to process incoming JSON data to required format */
function processData(data, rId){
    /* initializing variables */
    var nodes = new Set(),
        newNodes = [],
        links = [],
        topology = {};

    data.nodes.forEach(function(node){
        node.color = "default";
        if(node.id === rId){
            node.color = "green";
        }
    });

    newNodes = data.nodes;
    data.edges.forEach(function(row){
        links.push({ "networkid": row.id, "netmask": row.mask, "source": row.source.id, "target": row.destination.id, "cost": row.metric, "deleted": false, "type": row.type});
    });

    /* check for same nodes / targets - need to be removed*/
    for(var i = 0; i < links.length; i++){
        for(var j = i + 1; j < links.length; j++){
            if(links[i].source === links[j].target){
                if(links[i].target === links[j].source){
                  if(links[i].cost !== links[j].cost){
                    links[i].cost = links[i].cost + "/" + links[j].cost;
                  }
                    links[j].deleted = true;
                }
            }
        }
    }
    topology.nodes = newNodes;

    /* remove duplicate routes, connections */
    topology.links = links.filter(function(del){
        return (del.deleted === false);
    });

    d3.netJsonGraph( topology , {
        linkDistance: 50,
        charge: -200,
        circleRadius: 12,
        defaultStyle: false,
        labelDy: "-1.8em"
    });
}
