import 'dart:async';

import 'package:flutter/material.dart';
import 'package:google_maps_flutter/google_maps_flutter.dart';
import 'package:liplibrary/bloc.dart';
import 'package:liplibrary/data/land.dart';
import 'package:liplibrary/data/position.dart';
import 'package:liplibrary/util/functions.dart';

class MapViewer extends StatefulWidget {
  final LandDTO land;

  MapViewer(this.land);

  @override
  _MapViewerState createState() => _MapViewerState();
}

class _MapViewerState extends State<MapViewer> {
  final GlobalKey<ScaffoldState> _key = GlobalKey();

  Completer<GoogleMapController> _completer = Completer();
  GoogleMapController _mapController;
  Position position;
  CameraPosition _cameraPosition;
  MapType mapType;
  Set<Marker> _markersForMap = Set();
  @override
  void initState() {
    super.initState();
    print(
        '📯📯 polygon has 📯 ${widget.land.polygon.length} points. 📯 if > 2 must draw polygon');
    _getLocation();
    _setMarkers();
  }

  _getLocation() async {
    position = await bloc.getCurrentLocation();
    print(
        '💠💠💠 setting new camera position  💠💠💠 after getting current location ${position.coordinates}');
    _cameraPosition = CameraPosition(
      target: LatLng(position.coordinates[1], position.coordinates[0]),
      zoom: 12.0,
    );

    setState(() {});
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      key: _key,
      appBar: AppBar(
        title: Text('Land Map Viewer'),
        backgroundColor: Colors.brown,
        bottom: PreferredSize(
          preferredSize: Size.fromHeight(100),
          child: Column(
            children: <Widget>[
              Text(
                '${widget.land.name}',
                style: Styles.yellowBoldMedium,
              ),
              SizedBox(
                height: 8,
              ),
              Text(
                getFormattedAmount('${widget.land.originalValue}', context),
                style: Styles.whiteBoldSmall,
              ),
              SizedBox(
                height: 20,
              ),
            ],
          ),
        ),
      ),
      body: Stack(
        children: <Widget>[
          _cameraPosition == null
              ? Container()
              : GoogleMap(
                  initialCameraPosition: _cameraPosition,
                  mapType: mapType == null ? MapType.hybrid : mapType,
                  markers: _markersForMap,
                  polygons: polygons,
                  myLocationEnabled: true,
                  compassEnabled: true,
                  zoomGesturesEnabled: true,
                  rotateGesturesEnabled: true,
                  scrollGesturesEnabled: true,
                  tiltGesturesEnabled: true,
                  onMapCreated: (mapController) {
                    debugPrint(
                        '🔆🔆🔆🔆🔆🔆 onMapCreated ... markersMap ...  🔆🔆🔆🔆');
                    _completer.complete(mapController);
                    _mapController = mapController;
                    if (widget.land.polygon.isEmpty) {
                      print(
                          'No points in polygon ... 🌍 🌍 🌍  try to place map at current location');
                    } else {
                      if (widget.land.polygon.length < 3) {
                        _setMarkers();
                      } else {
                        _setMarkers();
                        print(
                            'points in polygon > 2 : ${widget.land.polygon.length}... 🌍 🌍 🌍  calling _drawPolygon');
                        _drawPolygon();
                      }
                    }
                  }),
        ],
      ),
    );
  }

  LatLng latLng;

  void _setMarkers() {
    _markersForMap.clear();
    if (widget.land.polygon.isEmpty) return;
    debugPrint(
        'Setting  🏮 🏮 ${widget.land.polygon.length} 🏮 🏮 markers on map');
    List<LatLng> points = List();
    var num = 0;
    widget.land.polygon.forEach((c) {
      points.add(LatLng(c.latitude, c.longitude));
    });
    points.forEach((p) {
      num++;
      var marker = Marker(
          onTap: () {
            debugPrint('marker tapped!! ❤️ 🧡 💛 :latLng: $p ❤️ 🧡 💛');
          },
          icon:
              BitmapDescriptor.defaultMarkerWithHue(BitmapDescriptor.hueOrange),
          markerId: MarkerId(DateTime.now().toIso8601String()),
          position: LatLng(p.latitude, p.longitude),
          infoWindow: InfoWindow(
              title: widget.land.name,
              snippet: 'Point #$num in polygon',
              onTap: () {
                debugPrint(
                    ' 🧩 🧩 🧩 infoWindow tapped  🧩 🧩 🧩 ${p.toString()}');
              }));
      _markersForMap.add(marker);
    });
    if (_mapController != null) {
      _mapController.animateCamera(CameraUpdate.newLatLngZoom(
          points[widget.land.polygon.length - 1], 14));
      setState(() {});
    }
  }

  Set<Polygon> polygons = Set();

  _drawPolygon() {
    if (widget.land.polygon.isEmpty) {
      debugPrint('🏮 🏮 _drawPolygon: NO POINTS TO DRAW 🏮 🏮 ');
      return;
    }
    debugPrint(
        'Drawing polygon from  🏮 🏮 ${widget.land.polygon.length} 🏮 🏮 points');
    polygons.clear();
    List<LatLng> points = List();
    widget.land.polygon.forEach((c) {
      points.add(LatLng(c.latitude, c.longitude));
    });
    var pol = Polygon(
        polygonId: PolygonId('${DateTime.now().microsecondsSinceEpoch}'),
        points: points,
        geodesic: true,
        strokeColor: Colors.yellow,
        fillColor: Colors.transparent);
    polygons.add(pol);
    setState(() {});
  }
}
