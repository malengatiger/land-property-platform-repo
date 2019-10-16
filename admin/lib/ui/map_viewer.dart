import 'dart:async';

import 'package:admin/ui/map_editor.dart';
import 'package:flutter/material.dart';
import 'package:google_maps_flutter/google_maps_flutter.dart';
import 'package:liplibrary/bloc.dart';
import 'package:liplibrary/data/land.dart';
import 'package:liplibrary/data/position.dart';
import 'package:liplibrary/util/functions.dart';
import 'package:liplibrary/util/slide_right.dart';

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
    _drawPolygon();
  }

  _getLocation() async {
    position = await bloc.getCurrentLocation();
    print(
        '💠💠💠 setting new camera position  💠💠💠 after getting current location ${position.coordinates}');
    _cameraPosition = CameraPosition(
      target: LatLng(position.coordinates[1], position.coordinates[0]),
      zoom: ZOOM,
    );

    setState(() {});
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      key: _key,
      appBar: AppBar(
        title: Text('Land Map Viewer'),
        actions: <Widget>[
          IconButton(
            onPressed: _edit,
            icon: Icon(Icons.edit),
          ),
        ],
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
                getFormattedAmount('${widget.land.value}', context),
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
                      print(
                          'points in polygon > 2 : ${widget.land.polygon.length}... 🌍 🌍 🌍  calling _drawPolygon');
                      _drawPolygon();
                    }
                  }),
        ],
      ),
    );
  }

  LatLng latLng;

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
    latLng = _computeCentroid();
    _animate();
    setState(() {});
  }

  void _edit() {
    Navigator.push(context, SlideRightRoute(widget: MapEditor(widget.land)));
  }

  void _animate() {
    var centre = _computeCentroid();
    if (_mapController != null) {
      _mapController.animateCamera(CameraUpdate.newLatLngZoom(centre, ZOOM));
      setState(() {});
    }
  }

  static const ZOOM = 12.0;
  LatLng _computeCentroid() {
    double latitude = 0;
    double longitude = 0;
    int n = widget.land.polygon.length;

    for (var coords in widget.land.polygon) {
      latitude += coords.latitude;
      longitude += coords.longitude;
    }

    return new LatLng(latitude / n, longitude / n);
  }
}
