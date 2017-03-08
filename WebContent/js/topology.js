"use strict";

var netModule = angular.module('topology', []);

netModule.controller('netgraph', function($rootScope, $scope, $http, $interval, $timeout) {

    $scope.buttonTitle = "Show Sync Tree";
    $scope.header = "Complete";
    $scope.responseReady = false;
    $scope.topology = true;

    /* load JSON with demo data */
    if($rootScope.isDemo){
        $scope.responseReady = true;
        $http.get('../demo/demo_topology.json')
            .then(function(res){
                $scope.topology = res.data;

                d3.netJsonGraph($scope.topology , {
                    linkDistance: 50,
                    charge: -200,
                    circleRadius: 12,
                    defaultStyle: false,
                    labelDy: "-1.8em"
                });

                //TESTING
                console.log(d3.selectAll("line"));
                var el = document.getElementsByClassName('10-255-255-3_10-255-255-4');//.style('stroke', '#f90808');
                console.log(el[0]);
                el[0].style.stroke = '#f90808';
            });
    }

    /* live data */
    else {

        $scope.SwitchView = function () {
            $scope.buttonTitle = $scope.buttonTitle === "Show Sync Tree" ? "Show Topology" : "Show Sync Tree";
            $scope.header = $scope.header === "Complete" ? "Sync Tree" : "Complete";

            //switch view active - loading topology
            if($scope.buttonTitle === "Show Sync Tree"){
                $scope.topology = true;
                getFullNetwork();

                // call get method every n seconds
                $scope.Timer = $interval(function() {
                    console.log(new Date());
                      $rootScope.connectionDetails.time = new Date().toLocaleString();
                      getFullNetwork();
                }, 25000);
            }

            else {
                console.log("Displaying sync tree, stopping the timer.");
                $scope.topology = false;
                $interval.cancel($scope.Timer);
                getSyncTree($rootScope.connectionDetails.routerid);

                $scope.changedSource = function(rootNode){
                    if(rootNode !== ''){
                        $scope.selectedRouter = rootNode;
                        getSyncTree($scope.selectedRouter);
                    }
                }

                //highlight shortest path when destination selected
                $scope.changedDestination = function(destination){
                    if(destination !== '' && $scope.selectedRouter){
                        if(destination === $scope.selectedRouter){
                            console.log("Same router selected.");
                            var links = d3.selectAll(".njg-link")[0];
                            for(var j = 0; j < links.length; j++) {
                                links[j].style.stroke = "#999";
                            }
                            $scope.distance = 0;
                        }
                        else {
                            //clear each time destination is changed
                            var links = d3.selectAll(".njg-link")[0];
                            for(var j = 0; j < links.length; j++) {
                                links[j].style.stroke = "#999";
                            }
                            getShortestPath($scope.selectedRouter, destination);
                        }
                    }
                }
            }
        }

        //same approach as in switch view where data is polled initially every n seconds
        console.log("Displaying full network.");
        //poll data very often after table deletion
        $interval(function(){
          if(!$scope.responseReady){
              getRouters();
              if($scope.buttonTitle === "Show Sync Tree"){
                  getFullNetwork();
                  console.log("WAITING ON FULL NETWORK 500ms.");
              }
              else {
                  getSyncTree($rootScope.connectionDetails.routerid);
                  console.log("WAITING ON SYNC TREE 500ms.");
              }
          }
        }, 1000);

        // call get method every n seconds
        $scope.Timer = $interval(function() {
            getFullNetwork();
        }, 25000);

    }

    /* cancel timer when changing view */
    $scope.$on("$destroy",function(){
        if (angular.isDefined($scope.Timer)) {
            $interval.cancel($scope.Timer);
            console.log("Timer was cancelled.");
        }
    });

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
                }
                else {
                  console.log("empty response due to possible overlap in execution time on the backend");
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
                console.log("Error retrieving full topology: " + error);
            });

    };

    /* Method to get the sync tree */
    function getSyncTree(routerId){

        var config = {
            method: 'GET',
            url: 'http://localhost:8080/topology/api/topology/synctree/' + routerId
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
                console.log("empty response due to possible overlap in execution time on the backend");
                if(d3.select("svg")){
                    d3.select("svg").remove();
                    d3.select(".njg-metadata").remove();
                }

                $scope.responseReady = false;
                $scope.background = "#fffff0";
              }
            })
            .catch(function(error){
                console.log("Error retrieving sync tree: " + error);
            });

    };

    /* Method to get the routers for selector*/
    function getRouters(){

        var config = {
            method: 'GET',
            url: 'http://localhost:8080/topology/api/type/distinctrouter'
        };
        $http(config)
            .then(function (response) {
                $scope.routers = response.data;
            })
            .catch(function(error){
                console.log("ERROR RETRIEVING ROUTER DATA: " + error);
            });

    };

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

                  routers = Object.keys(resp).sort(function(a,b){ return resp[a]-resp[b] });

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
                  console.log("Unsuccesfull. Updating tables.")
                }
            })
            .catch(function(error){
                console.log("ERROR RETRIEVING SHORTEST PATH DATA: " + error);
            });

    };
});

function processData(data, rId){
    /* initializing variables */
    var nodes = new Set(),
        newNodes = [],
        links = [],
        otherLinks = [],
        newLinks,
        topology = {
            "type": "",
            "label":"",
            "protocol":"",
            "version": "",
            "revision": "",
            "metric": "",
            "router_id":"",
            "nodes": "",
            "links": ""
        };

    data.nodes.forEach(function(node){
        node.color = "default";
        if(node.id === rId){
            node.color = "green";
        }
    })

    newNodes = data.nodes;
    data.edges.forEach(function(row){
        links.push({ "source": row.source.id, "target": row.destination.id, "cost": row.metric, "deleted": false});
    })

    /* check for same nodes / targets - need to be removed*/
    for(var i = 0; i < links.length; i++){
        for(var j = i + 1; j < links.length; j++){
            if(links[i].source === links[j].target){
                if(links[i].target === links[j].source){
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
        linkClassProperty: "type",
        nodeClassProperty: "gateway",
        labelDy: "-1.8em"
    });
}
