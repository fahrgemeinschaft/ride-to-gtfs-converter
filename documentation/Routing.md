# Routing tool evaluation

We work with routing to generate in between stops where passengers can potentially join a ride. This must be coordinated with the driver to arrange an additional pickup or drop-off on the way. 3 tools will be evaluated using an example ride from Berlin to Hamburg.

**Overview**

| service          | total distance | travel time | amount of stops | average distance between stops | request time | prices                                                                               |
|------------------|----------------|-------------|-----------------|--------------------------------|--------------|--------------------------------------------------------------------------------------|
| OSRM             | 280 km         | 3,1 h       | 32/239/2.644/.. | 8.766/1.173/106 m              | 6.200 ms     | free commercial usage                                                                |
| GraphHopper      | 281 km         | 2,8 h       | 23/1432         | 12.204/196 m                   | 610 ms       | registration required, commercial use starting from 48 €/month for 1666 requests/day |
| openrouteservice | 280 km         | 2,9 h       | 27/2086         | 10.383/134 m                   | 6.600 ms     | free commercial usage up to 2.000 requests/day                                       |

Other pay services are provided by
* Google Maps
* HERE
* Mapbox
* Microsoft
* TomTom
* MapQuest
* ArcGIS
...

### OSRM

* GitHub: [https://github.com/Project-OSRM/osrm-backend](https://github.com/Project-OSRM/osrm-backend)
* API documentation: [http://project-osrm.org/docs/v5.22.0/api/](http://project-osrm.org/docs/v5.22.0/api/)

The OSRM routing response has different collections of generated in between stops as listed in the table.

| stops type | generated in                                     | amount |
|------------|--------------------------------------------------|--------|
| 1          | route.leg.step.maneuver.location                 | 32     |
| 2          | route.leg.step.intersection.location             | 239    |
| 3          | route.geometry.coordinates                       | 2612   |
| 4          | route.leg.step.geometry.coordinates              | 2644   |
| 5          | convertOsmIdToLatLon(route.leg.annotation.node)  | 2612   |

**stops type 1**
![](images/osrm-route-leg-step-maneuver-location.png "")

**stops type 2**
![](images/osrm-route-leg-step-intersection-location.png "")

**stops type 3**
![](images/osrm-route-geometry-coordinates.png "")

**stops type 4**
![](images/osrm-route-leg-step-geometry-coordinates.png "")

**stops type 5**
![](images/osrm-route-leg-annotation-node-convertOsmIdToLatLon.png "")

The first Google Earth picture shows some stops close to the origin and the destination, but not so many in between. In the second picture the stops are equally spread along the route. The amount of stops is more reasonable for our use case than in the last three pictures.

**stops type 1**

<font size="1">

| coordinates                      | osm_type | extent                                           | osm_key | housenumber | city          | street                   | osm_value         | postcode | name                     | state       |
|----------------------------------|----------|--------------------------------------------------|---------|-------------|---------------|--------------------------|-------------------|----------|--------------------------|-------------|
| [13.3816639, 52.5316801]         | N        |                                                  | railway |             | Berlin        | Chausseestraße           | subway_entrance   | 10115    | U Naturkundemuseum       | Berlin      |
| [13.3717709, 52.5390625]         | W        | [13.3717709, 52.5390625, 13.3719206, 52.5389596] | highway |             | Berlin        |                          | secondary         | 13353    | Pankenbrücke             | Berlin      |
| [13.3321246, 52.5619817]         | N        |                                                  | place   | 1           | Berlin        | Scharnweberstraße        | house             | 13405    |                          | Berlin      |
| [13.3306441, 52.5626152]         | W        | [13.3304715, 52.5626152, 13.3306441, 52.5625513] | place   |             | Berlin        |                          | postcode          | 13405    |                          | Berlin      |
| [13.3267151, 52.562555]          | W        | [13.3265022, 52.562555, 13.3267151, 52.5625105]  | highway |             | Berlin        |                          | primary           | 13405    | Kapweg                   | Berlin      |
| [13.3224464, 52.5606162]         | W        | [13.3222432, 52.5607013, 13.3230448, 52.5605885] | highway |             | Berlin        |                          | primary           | 13405    | Kurt-Schumacher-Damm     | Berlin      |
| [13.3123812, 52.5648813]         | N        |                                                  | highway |             | Berlin        |                          | motorway_junction | 13405    | Eichborndamm             | Berlin      |
| [13.3063887, 52.5674806]         | N        |                                                  | highway |             | Berlin        |                          | motorway_junction | 13405    | Kurt-Schumacher-Platz    | Berlin      |
| [13.2006461, 52.6957881]         | N        |                                                  | highway |             | Velten        |                          | motorway_junction | 16767    | Kreuz Oranienburg        | Brandenburg |
| [13.1764499, 52.7058281]         | W        | [13.1711161, 52.7058281, 13.1764499, 52.705786]  | highway |             | Velten        |                          | motorway          | 16727    | Nördlicher Berliner Ring | Brandenburg |
| [13.0308651, 52.7098808]         | N        |                                                  | highway |             | Oberkrämer    | Westlicher Berliner Ring | motorway_junction | 16727    | Dreieck Havelland        | Brandenburg |
| [12.4624524, 53.1232044]         | N        |                                                  | highway |             | Heiligengrabe |                          | motorway_junction | 16909    | Dreieck Wittstock/Dosse  | Brandenburg |
| [10.0698666, 53.5596599]         | W        | [10.0696875, 53.5596673, 10.0699588, 53.5596305] | highway |             | Hamburg       |                          | primary           | 22111    | Sievekingsallee          | Hamburg     |
| [10.0695751, 53.5596008]         | W        | [10.0693722, 53.5596305, 10.0696875, 53.5595187] | highway |             | Hamburg       |                          | primary           | 22111    | Sievekingsallee          | Hamburg     |
| [10.0534485, 53.5610705]         | W        | [10.0533592, 53.5611762, 10.0535006, 53.5610263] | highway |             | Hamburg       |                          | residential       | 20535    | Schulenbeksweg           | Hamburg     |
| [10.0419958, 53.555555]          | W        | [10.0419958, 53.555555, 10.0425213, 53.5555075]  | highway |             | Hamburg       |                          | secondary         | 20537    | Hammer Landstraße        | Hamburg     |
| [10.0406689, 53.5555759]         | W        | [10.0404272, 53.5555759, 10.0406689, 53.5555599] | highway |             | Hamburg       |                          | secondary         | 20537    | Hammer Landstraße        | Hamburg     |
| [10.0265681, 53.5538048]         | W        | [10.0265681, 53.5538733, 10.0265893, 53.5538048] | highway |             | Hamburg       |                          | residential       | 20537    | Klaus-Groth-Straße       | Hamburg     |
| [10.0243027, 53.5535981]         | W        | [10.0243027, 53.5536096, 10.0244424, 53.5535981] | highway |             | Hamburg       |                          | secondary         | 20099    | Berlinertordamm          | Hamburg     |
| [10.0192246, 53.5527811]         | W        | [10.0188015, 53.5527811, 10.0192246, 53.5526661] | highway |             | Hamburg       |                          | secondary         | 20097    | Kurt-Schumacher-Allee    | Hamburg     |
| [10.0185868, 53.5526014]         | W        | [10.0182237, 53.5526014, 10.0185868, 53.5524824] | highway |             | Hamburg       |                          | secondary         | 20097    | Kurt-Schumacher-Allee    | Hamburg     |
| [10.017895, 53.5527903]          | W        | [10.0177779, 53.5528896, 10.0179188, 53.5527418] | highway |             | Hamburg       |                          | residential       | 20097    | Adenauerallee            | Hamburg     |
| [10.0088771, 53.5522089]         | W        | [10.0087788, 53.5522089, 10.0088771, 53.5521905] | highway |             | Hamburg       |                          | secondary         | 20099    | Steintorplatz            | Hamburg     |
| [10.005776522160406, 53.5520385] | W        | [10.0056825, 53.5520824, 10.0058713, 53.5519865] | shop    |             | Hamburg       | Wallringtunnel           | bakery            | 20095    | Tunnel Bäcker            | Hamburg     |
| [10.0056237, 53.552885]          | N        |                                                  | amenity |             | Hamburg       | Wallringtunnel           | cafe              | 20095    | Starbucks                | Hamburg     |
| [10.0038483, 53.5541033]         | W        | [10.0036695, 53.5542345, 10.0038483, 53.5541033] | highway |             | Hamburg       |                          | secondary         | 20095    | Glockengießerwall        | Hamburg     |
| [10.0025086, 53.5534776]         | W        | [10.0015284, 53.5540275, 10.0025086, 53.5534776] | highway |             | Hamburg       |                          | unclassified      | 20095    | Brandsende               | Hamburg     |
| [9.999922, 53.5549622]           | W        | [9.999922, 53.5552167, 10.0002613, 53.5549622]   | highway |             | Hamburg       |                          | tertiary          | 20095    | Ballindamm               | Hamburg     |
| [9.9945103, 53.552045]           | W        | [9.9943859, 53.5520915, 9.9946316, 53.5520249]   | highway |             | Hamburg       |                          | tertiary          | 20095    | Jungfernstieg            | Hamburg     |
| [9.9951072, 53.5511752]          | W        | [9.9951072, 53.551472, 9.9956683, 53.5511752]    | highway |             | Hamburg       |                          | pedestrian        | 20095    | Hermannstraße            | Hamburg     |
| [9.9942354, 53.5506939]          | W        | [9.9939492, 53.5508649, 9.9942354, 53.5506939]   | highway |             | Hamburg       |                          | unclassified      | 20095    | Rathausmarkt             | Hamburg     |
| [9.9936264, 53.5510823]          | W        | [9.9936264, 53.5510823, 9.9938776, 53.5509131]   | highway |             | Hamburg       |                          | unclassified      | 20095    | Rathausmarkt             | Hamburg     |

</font>

### GraphHopper

| stops type | generated in                | amount |
|------------|-----------------------------|--------|
| 1          | paths-instructions-interval | 23     |
| 2          | paths-points-coordinates    | 1432   |

**stops type 1**
![](images/gh-paths-instructions-interval.png "")

**stops type 2**
![](images/gh-paths-points-coordinates.png "")

### openrouteservice

* GitHub: [https://github.com/GIScience/openrouteservice](https://github.com/GIScience/openrouteservice)
* API documentation: [https://openrouteservice.org/dev/#/api-docs/directions/get](https://openrouteservice.org/dev/#/api-docs/directions/get)
