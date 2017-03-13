(function () {

    d3._extend = function(defaults, options) {
        var extended = {},
            prop;
        for(prop in defaults) {
            if(Object.prototype.hasOwnProperty.call(defaults, prop)) {
                extended[prop] = defaults[prop];
            }
        }
        for(prop in options) {
            if(Object.prototype.hasOwnProperty.call(options, prop)) {
                extended[prop] = options[prop];
            }
        }
        return extended;
    };

    /**
      * @function
      *   @name d3._pxToNumber
      * Convert strings like "10px" to 10
      *
      * @param  {string}       val The value to convert
      * @return {int}              The converted integer
      */
    d3._pxToNumber = function(val) {
        return parseFloat(val.replace('px'));
    };

    /**
      * @function
      *   @name d3._windowHeight
      *
      * Get window height
      *
      * @return  {int}            The window innerHeight
      */
    d3._windowHeight = function() {
        return window.innerHeight || document.documentElement.clientHeight || 600;
    };

    /**
      * @function
      *   @name d3._getPosition
      *
      * Get the position of `element` relative to `container`
      *
      * @param  {object}      element
      * @param  {object}      container
      */

     d3._getPosition = function(element, container) {
         var n = element.node(),
             nPos = n.getBoundingClientRect();
             cPos = container.node().getBoundingClientRect();
         return {
            top: nPos.top - cPos.top,
            left: nPos.left - cPos.left,
            width: nPos.width,
            bottom: nPos.bottom - cPos.top,
            height: nPos.height,
            right: nPos.right - cPos.left
        };
     };

    /**
     * netjsongraph.js main function
     *
     * @constructor
     * @param  {string}      url             The NetJSON file url
     * @param  {object}      opts            The object with parameters to override {@link d3.netJsonGraph.opts}
     */
    d3.netJsonGraph = function(url, opts) {
        /**
         * Default options
         *
         * @param  {string}     el                  "body"      The container element                                  el: "body" [description]
         * @param  {bool}       metadata            true        Display NetJSON metadata at startup?
         * @param  {bool}       defaultStyle        true        Use css style?
         * @param  {bool}       animationAtStart    false       Animate nodes or not on load
         * @param  {array}      scaleExtent         [0.25, 5]   The zoom scale's allowed range. @see {@link https://github.com/mbostock/d3/wiki/Zoom-Behavior#scaleExtent}
         * @param  {int}        charge              -130        The charge strength to the specified value. @see {@link https://github.com/mbostock/d3/wiki/Force-Layout#charge}
         * @param  {int}        linkDistance        50          The target distance between linked nodes to the specified value. @see {@link https://github.com/mbostock/d3/wiki/Force-Layout#linkDistance}
         * @param  {float}      linkStrength        0.2         The strength (rigidity) of links to the specified value in the range. @see {@link https://github.com/mbostock/d3/wiki/Force-Layout#linkStrength}
         * @param  {float}      friction            0.9         The friction coefficient to the specified value. @see {@link https://github.com/mbostock/d3/wiki/Force-Layout#friction}
         * @param  {string}     chargeDistance      Infinity    The maximum distance over which charge forces are applied. @see {@link https://github.com/mbostock/d3/wiki/Force-Layout#chargeDistance}
         * @param  {float}      theta               0.8         The Barnes–Hut approximation criterion to the specified value. @see {@link https://github.com/mbostock/d3/wiki/Force-Layout#theta}
         * @param  {float}      gravity             0.1         The gravitational strength to the specified numerical value. @see {@link https://github.com/mbostock/d3/wiki/Force-Layout#gravity}
         * @param  {int}        circleRadius        8           The radius of circles (nodes) in pixel
         * @param  {string}     labelDx             "0"         SVG dx (distance on x axis) attribute of node labels in graph
         * @param  {string}     labelDy             "-1.3em"    SVG dy (distance on y axis) attribute of node labels in graph
         * @param  {function}   onInit                          Callback function executed on initialization
         * @param  {function}   onLoad                          Callback function executed after data has been loaded
         * @param  {function}   onEnd                           Callback function executed when initial animation is complete
         * @param  {function}   linkDistanceFunc                By default high density areas have longer links
         * @param  {function}   redraw                          Called when panning and zooming
         * @param  {function}   prepareData                     Used to convert NetJSON NetworkGraph to the javascript data
         * @param  {function}   onClickNode                     Called when a node is clicked
         * @param  {function}   onClickLink                     Called when a link is clicked
         */

        opts = d3._extend({
            el: "#graph",
            metadata: true,
            defaultStyle: true,
            animationAtStart: true,
            scaleExtent: [0.25, 5],
            charge: -200, //-130 originally
            linkDistance: 50,
            linkStrength: 0.2,
            friction: 0.9,  // d3 default
            chargeDistance: Infinity,  // d3 default
            theta: 0.8,  // d3 default
            gravity: 0.1,
            circleRadius: 8,
            labelDx: "0",
            labelDy: "-1.3em",
            nodeClassProperty: null,
            linkClassProperty: null,

            //Callback function executed on initialization
            onInit: function(url, opts) {},

            //Callback function executed after data has been loaded
            onLoad: function(url, opts) {},

            //Callback function executed when initial animation is complete
            onEnd: function(url, opts) {},

            //high density areas have longer links
            linkDistanceFunc: function(d){
                var val = opts.linkDistance;
                if(d.source.linkCount >= 4 && d.target.linkCount >= 4) {
                    return val * 2;
                }
                return val;
            },

            //Called on zoom and pan
            redraw: function() {
                panner.attr("transform",
                    "translate(" + d3.event.translate + ") " +
                    "scale(" + d3.event.scale + ")"
                );
            },

            //Convert NetJSON NetworkGraph to the data structure consumed by d3
            prepareData: function(graph) {
                var nodesMap = {},
                    nodes = graph.nodes.slice(), // copy
                    links = graph.links.slice(), // copy
                    nodes_length = graph.nodes.length,
                    links_length = graph.links.length;

                for(var i = 0; i < nodes_length; i++) {
                    // count how many links every node has
                    nodes[i].linkCount = 0;
                    nodesMap[nodes[i].id] = i;
                }
                for(var c = 0; c < links_length; c++) {
                    var sourceIndex = nodesMap[links[c].source],
                    targetIndex = nodesMap[links[c].target];
                    // ensure source and target exist
                    if(!nodes[sourceIndex]) { throw("source '" + links[c].source + "' not found"); }
                    if(!nodes[targetIndex]) { throw("target '" + links[c].target + "' not found"); }
                    links[c].source = nodesMap[links[c].source];
                    links[c].target = nodesMap[links[c].target];
                    // add link count to both ends
                    nodes[sourceIndex].linkCount++;
                    nodes[targetIndex].linkCount++;
                }
                return { "nodes": nodes, "links": links };
            },

            //Called on a node click
            onClickNode: function(n) {
                var overlay = d3.select(".njg-overlay"),
                    overlayInner = d3.select(".njg-overlay > .njg-inner"),
                    html = "";


                if(n.type === "router"){
                    html += "<p><b>Router ID: </b>" + n.id + "</p>";
                    html += "<p><b>Interfaces: </b></p>";

                    if(n.interf){
                        var inter = n.interf.split(",");
                        for(var i in inter){
                            html += "<p>&emsp;" + inter[i] + "</p>"
                        }
                    }
                }
                if(n.type === "external"){
                    html += "<p><b>External ID: </b>" + n.id + "</p>";
                    html += "<p><b>External Routes: </b></p>";

                    if(n.interf){
                        var inter = n.interf.split(",");
                        for(var i in inter){
                            html += "<p>&emsp;" + inter[i] + "</p>"
                        }
                    }
                }
                if(n.type === "switch"){
                    html += "<p><b>Switch ID: </b>" + n.id + "</p>";
                }
                if(n.linkCount) {
                    html += "<p><b>Links</b>: " + n.linkCount + "</p>";
                }

                overlayInner.html(html);
                overlay.classed("njg-hidden", false);
                overlay.style("display", "block");
                // set "open" class to current node
                removeOpenClass();
                d3.select(this).classed("njg-open", true);
            },

            //Called on link click
            onClickLink: function(l) {
                var overlay = d3.select(".njg-overlay"),
                    overlayInner = d3.select(".njg-overlay > .njg-inner"),
                    html = "<p><b>Connected routers:</b></p>"
                    html += "<p>&emsp;" + (l.source.label || l.source.id) + "</p>";
                    html += "<p>&emsp;" + (l.target.label || l.target.id) + "</p>";
                    html += "<p><b>Cost</b>: " + l.cost + "</p>";
                if(l.properties) {
                    for(var key in l.properties) {
                        if(!l.properties.hasOwnProperty(key)) { continue; }
                        html += "<p><b>"+ key.replace(/_/g, " ") +"</b>: " + l.properties[key] + "</p>";
                    }
                }
                overlayInner.html(html);
                overlay.classed("njg-hidden", false);
                overlay.style("display", "block");
                // set "open" class to current link
                removeOpenClass();
                d3.select(this).classed("njg-open", true);
            }
        }, opts);

        // init callback
        opts.onInit(url, opts);

        if(!opts.animationAtStart) {
            opts.linkStrength = 2;
            opts.friction = 0.3;
            opts.gravity = 0;
        }

        if(opts.el == "body") {
            var body = d3.select(opts.el),
                rect = body.node().getBoundingClientRect();
            if (d3._pxToNumber(d3.select("body").style("height")) < 60) {
                body.style("height", d3._windowHeight() - rect.top - rect.bottom  + "px");
            }
        }

        var el = d3.select(opts.el).style("position", "relative"),
            width = d3._pxToNumber(el.style('width')),
            height = d3._pxToNumber(el.style('height')),
            force = d3.layout.force()
                      .charge(opts.charge)
                      .linkStrength(opts.linkStrength)
                      .linkDistance(opts.linkDistanceFunc)
                      .friction(opts.friction)
                      .chargeDistance(opts.chargeDistance)
                      .theta(opts.theta)
                      .gravity(opts.gravity)
                      // width is easy to get, if height is 0 take the height of the body
                      .size([width, height]),
            zoom = d3.behavior.zoom().scaleExtent(opts.scaleExtent),
            // panner is the element that allows zooming and panning
            panner = el.append("svg")
                       .attr("width", width)
                       .attr("height", height)
                       .call(zoom.on("zoom", opts.redraw))
                       .append("g")
                       .style("position", "absolute"),
            svg = d3.select(opts.el + " svg"),
            drag = force.drag().on("dragstart",dragstart),
            overlay = d3.select(opts.el).append("div").attr("class", "njg-overlay"),
            closeOverlay = overlay.append("a").attr("class", "njg-close"),
            overlayInner = overlay.append("div").attr("class", "njg-inner"),
            metadata = d3.select(opts.el).append("div").attr("class", "njg-metadata"),
            metadataInner = metadata.append("div").attr("class", "njg-inner"),
            closeMetadata = metadata.append("a").attr("class", "njg-close"),

            //Remove open classes from nodes and links
            removeOpenClass = function () {
                d3.selectAll("svg .njg-open").classed("njg-open", false);
            };
            processJson = function(graph) {
                //Init netJsonGraph
                init = function(url, opts) {
                    d3.netJsonGraph(url, opts);
                };

                //Remove all instances
                destroy = function() {
                    force.stop();
                    d3.select("#selectGroup").remove();
                    d3.select(".njg-overlay").remove();
                    d3.select(".njg-metadata").remove();
                    overlay.remove();
                    overlayInner.remove();
                    metadata.remove();
                    svg.remove();
                    node.remove();
                    link.remove();
                    nodes = [];
                    links = [];
                };

                //Destroy and e-init all instances
                reInit = function() {
                    destroy();
                    init(url, opts);
                };

                var data = opts.prepareData(graph),
                    links = data.links,
                    nodes = data.nodes;

                // disable transitions during drag
                drag.on('dragstart', function(n){
                    d3.event.sourceEvent.stopPropagation();
                    zoom.on('zoom', null);
                    d3.select(this).classed("fixed", n.fixed = true);
                })
                // re-enable transitions drag stop
                .on('dragend', function(n){
                    zoom.on('zoom', opts.redraw);
                })
                .on("drag", function(d) {
                    // avoid pan & drag conflict
                    d3.select(this).attr("x", d.x = d3.event.x).attr("y", d.y = d3.event.y);
                });

                force.nodes(nodes).links(links).start();

                var link = panner.selectAll(".link")
                                 .data(links)
                                 .enter().append("line")
                                 .attr("class", function (link) {
                                     var baseClass = "njg-link ",
                                         //add property to each link to know which nodes they connect
                                         addClass = link.source.id.replace(/\./g, '-') + '_' + link.target.id.replace(/\./g, '-');
                                         addClassType = link.type;

                                     return baseClass + addClass + " " + addClassType;
                                 })
                                 .on("click", opts.onClickLink),
                    //create nodes with respective classes and graphical elements
                    groups = panner.selectAll(".node")
                                   .data(nodes)
                                   .enter()
                                   .append("g")
                                   //add g class according to device typeof
                                   .attr("class", function(node){
                                      var gClass = node.type;
                                      return gClass;
                                   });
                    node = groups.append("circle")
                                 .attr("class", function (node) {
                                      var baseClass = "njg-node";

                                     return baseClass +  " " + "color-" + node.color;
                                 })
                                 .attr("r", opts.circleRadius)
                                 .attr("id", function(d, i) { return d.type })
                                 .on("click", opts.onClickNode)
                                 .call(drag),

                    labels = groups.append('text')
                                   .text(function(n){ return n.label || n.id })
                                   .attr('dx', opts.labelDx)
                                   .attr('dy', opts.labelDy)
                    .attr('class', 'njg-tooltip');

                // Close overlay
                closeOverlay.on("click", function() {
                    removeOpenClass();
                    overlay.classed("njg-hidden", true);
                });
                // Close metadata panel
                closeMetadata.on("click", function() {
                    removeOpenClass();
                    metadata.classed("njg-hidden", true);
                });
                // default style - possibility to implement own style
                if(opts.defaultStyle) {
                    var colors = d3.scale.category20c();

                    node.style({
                        "fill": function(d){ return colors(d.linkCount); },
                        "cursor": "pointer"
                    });
                }
                // metadata style hide after 10 seconds
                if(opts.metadata) {
                    metadata.attr("class", "njg-metadata").style("display", "block");
                    setTimeout(function() {
                        removeOpenClass();
                        metadata.classed("njg-hidden", true);
                    }, 10000);
                }

                //inner html to add more info to metadata panel
                var html = "";

                // Add nodes and links count
                html += "<p><b>Nodes: </b><span>" + graph.nodes.length + "</span></p>";
                html += "<p><b>Links: </b><span>" + graph.links.length + "</span></p>";
                metadataInner.html(html);
                metadata.classed("njg-hidden", false);

                // onLoad callback
                opts.onLoad(url, opts);

                force.on("tick", function() {

                    link.attr("x1", function(d) {
                        return d.source.x;
                    })
                    .attr("y1", function(d) {
                        return d.source.y;
                    })
                    .attr("x2", function(d) {
                        return d.target.x;
                    })
                    .attr("y2", function(d) {
                        return d.target.y;
                    });

                    node.attr("cx", function(d) {
                        return d.x;
                    })
                    .attr("cy", function(d) {
                        return d.y;
                    });

                    labels.attr("transform", function(d) {
                        return "translate(" + d.x + "," + d.y + ")";
                    });
                })
                .on("end", function(){
                    force.stop();
                    // onEnd callback
                    opts.onEnd(url, opts);
                });

                return force;
            };

        if(typeof(url) === "object") {
            processJson(url);
        }
        else {
            //Parse the provided json file and call processJson() function
            d3.json(url, function(error, graph) {
                if(error) {
                    throw error;
                }
                else {
                    processJson(graph);
                }
            });
        }
    };

    //pin the node to fixed position
    function dragstart(d) {
        d3.select(this).classed("fixed", d.fixed = true);
    }
})();
