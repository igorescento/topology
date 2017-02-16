var netModule = angular.module('topology', []);

netModule.controller('netgraph', function($rootScope, $scope, $http) {

    /* load JSON with demo data */
    if($rootScope.isDemo){
        $http.get('../demo/demo_topology.json')
       .then(function(res){
          $scope.topology = res.data;

          d3.netJsonGraph( $scope.topology , {
             linkDistance: 200,
             charge: -200,
             circleRadius: 12,
             defaultStyle: false,
             labelDy: "-1.8em"
             });
        });
    }

    else {
      /* config to fetch live data from DB */
      var config = {
          method: 'GET',
          url: 'http://localhost:8080/topology/api/topology'
      };
      $http(config)
          .then(function (response) {
              processData(response.data);
          })
          .catch(function(error){
              console.log("ERROR RETRIEVING DATA: " + error);
          });
    };

      function processData(data){
          console.log("Loading live material.");
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

          /* get unique routers */
          data.forEach(function(row){
              nodes.add(row.routerid);
          });

          nodes.forEach(function(node){
              newNodes.push({ "id": node, "label": node, "type": "router"});
          });

          /* create links where num routers on network is 2 */
          data.forEach(function(row){
              if(row.numrouters == 2){
                  links.push({ "source": row.routerid, "target": row.routersid, "cost": row.metric, "deleted": false});
              }
              else{
                  if(newNodes.some(function(e){ return e.id === row.networkaddr })){

                  }
                  else {
                      newNodes.push({ "id": row.networkaddr, "label": row.networkaddr, "type": "switch"});
                  }
                  links.push({ "source": row.networkaddr, "target": row.routerid, "cost": row.metric/2, "deleted": false});
              }
          });

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
              linkDistance: 200,
              charge: -200,
              circleRadius: 12,
              defaultStyle: false,
              linkClassProperty: "type",
              nodeClassProperty: "gateway",
              labelDy: "-1.8em"
          });
      }

});
