import 'dart:async';

import 'package:admin/ui/map_viewer.dart';
import 'package:flutter/material.dart';
import 'package:google_maps_flutter/google_maps_flutter.dart';
import 'package:liplibrary/bloc.dart';
import 'package:liplibrary/data/coords.dart';
import 'package:liplibrary/data/land.dart';
import 'package:liplibrary/data/position.dart';
import 'package:liplibrary/util/functions.dart';
import 'package:liplibrary/util/slide_right.dart';
import 'package:liplibrary/util/snack.dart';
import 'package:liplibrary/util/znetwork.dart';

class MapEditor extends StatefulWidget {
  final LandDTO land;

  MapEditor(this.land);

  @override
  _MapEditorState createState() => _MapEditorState();
}

class _MapEditorState extends State<MapEditor> {
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
        title: Text('Land Map Editor'),
        actions: <Widget>[
          IconButton(
            icon: Icon(Icons.cancel),
            onPressed: _confirmRemovePolygons,
          ),
          IconButton(
            icon: Icon(Icons.create_new_folder),
            onPressed: _drawPolygon,
          ),
        ],
        bottom: PreferredSize(
          preferredSize: Size.fromHeight(60),
          child: Column(
            children: <Widget>[
              Text(
                '${widget.land.name}',
                style: Styles.blackBoldMedium,
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
                  onLongPress: _onMapLongPressed,
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
          widget.land.polygon.isEmpty || widget.land.polygon.length < 3
              ? Container()
              : Positioned(
                  right: 20,
                  bottom: 20,
                  child: isSubmitting == false
                      ? RaisedButton(
                          color: Colors.pink[700],
                          elevation: 16,
                          onPressed: _confirmSave,
                          child: Padding(
                            padding: const EdgeInsets.all(8.0),
                            child: Text(
                              'Submit Land Parcel',
                              style: Styles.whiteSmall,
                            ),
                          ),
                        )
                      : Container())
        ],
      ),
    );
  }

  LatLng latLng;

  void _onMapLongPressed(LatLng p) {
    print('🥏 Map long pressed 🥏 🥏 $p ...');
    latLng = p;
    showDialog(
        context: context,
        barrierDismissible: false,
        builder: (_) => new AlertDialog(
              title: new Text(
                "Confirm Point",
                style: Styles.blackBoldLarge,
              ),
              content: Container(
                height: 40.0,
                child: Column(
                  children: <Widget>[
                    Text(
                      widget.land == null ? '' : widget.land.name,
                      style: Styles.blackSmall,
                    ),
                  ],
                ),
              ),
              actions: <Widget>[
                FlatButton(
                  child: Text(
                    'NO',
                    style: TextStyle(color: Colors.grey),
                  ),
                  onPressed: () {
                    Navigator.pop(context);
                  },
                ),
                Padding(
                  padding: const EdgeInsets.only(bottom: 20.0),
                  child: RaisedButton(
                    onPressed: () {
                      print('🍏 onPressed');
                      _addPointToPolygon();
                    },
                    elevation: 4.0,
                    color: Colors.pink.shade700,
                    child: Padding(
                      padding: const EdgeInsets.all(16.0),
                      child: Text(
                        'Add to Polygon',
                        style: TextStyle(color: Colors.white),
                      ),
                    ),
                  ),
                ),
              ],
            ));
  }

  _addPointToPolygon() async {
    print('🔸🔸🔸 _addPointToPolygon: $latLng');
    Navigator.pop(context);

    AppSnackbar.showSnackbarWithProgressIndicator(
        scaffoldKey: _key,
        message: 'Adding point to polygon',
        textColor: Colors.blue,
        backgroundColor: Colors.black);

    try {
      if (widget.land.polygon == null) {
        widget.land.polygon = List();
      }
      widget.land.polygon.add(CoordinatesDTO(
          latitude: latLng.latitude, longitude: latLng.longitude));
      _key.currentState.removeCurrentSnackBar();
      print(
          "🔸🔸🔸 🔸🔸🔸 Point added to polygon, points: ${widget.land.polygon.length} ");
      for (var value in widget.land.polygon) {
        CoordinatesDTO coords = value;
        print("🔸🔸🔸 Point Coords: ${coords.toJson()}");
      }
      _placeNewMarker();
      _setMarkers();
      setState(() {});
      if (widget.land.polygon.length > 2) {
        _drawPolygon();
      }
    } catch (e) {
      AppSnackbar.showErrorSnackbar(
          scaffoldKey: _key, message: 'Error adding point', actionLabel: 'Err');
    }
  }

  void _placeNewMarker() {
    var marker = Marker(
        onTap: () {
          debugPrint('marker tapped!! ❤️ 🧡 💛 :latLng: $latLng ');
        },
        icon: BitmapDescriptor.defaultMarkerWithHue(BitmapDescriptor.hueOrange),
        markerId: MarkerId(DateTime.now().toIso8601String()),
        position: LatLng(latLng.latitude, latLng.longitude),
        infoWindow: InfoWindow(
            title: '${DateTime.now().toIso8601String()}',
            snippet: 'Point in Land Polygon',
            onTap: () {
              debugPrint(' 🧩 🧩 🧩 infoWindow tapped  🧩 🧩 🧩 ');
            }));
    _markersForMap.add(marker);
  }

  void _setMarkers() {
    _markersForMap.clear();
    if (widget.land.polygon.isEmpty) return;
    debugPrint(
        'Setting  🏮 🏮 ${widget.land.polygon.length} 🏮 🏮 markers on map');
    List<LatLng> points = List();
    widget.land.polygon.forEach((c) {
      points.add(LatLng(c.latitude, c.longitude));
    });
    points.forEach((p) {
      var marker = Marker(
          onTap: () {
            debugPrint('marker tapped!! ❤️ 🧡 💛 :latLng: $latLng ');
          },
          icon:
              BitmapDescriptor.defaultMarkerWithHue(BitmapDescriptor.hueOrange),
          markerId: MarkerId(DateTime.now().toIso8601String()),
          position: LatLng(p.latitude, p.longitude),
          infoWindow: InfoWindow(
              title: '${DateTime.now().toIso8601String()}',
              snippet: 'Point in Land Polygon',
              onTap: () {
                debugPrint(' 🧩 🧩 🧩 infoWindow tapped  🧩 🧩 🧩 ');
                _removePoint(p);
              }));
      _markersForMap.add(marker);
    });
    if (_mapController != null) {
      _mapController.animateCamera(CameraUpdate.newLatLngZoom(
          points[widget.land.polygon.length - 1], 12));
      setState(() {});
    }
  }

  void _removePoint(LatLng point) {
    print(' 🧩 🧩 🧩 ......... Remove point $point');
    int index = 0;
    var found = false;
    CoordinatesDTO coordinates = null;

    for (CoordinatesDTO p in widget.land.polygon) {
      if (p.latitude == point.latitude && p.longitude == point.longitude) {
        coordinates = p;
        break;
      }
    }
    if (coordinates != null) {
      widget.land.polygon.remove(coordinates);
      _setMarkers();
      _drawPolygon();
      setState(() {});
    }
  }

//  List<LatLng> points = List();
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

  void _confirmRemovePolygons() {
    showDialog(
        context: context,
        barrierDismissible: false,
        builder: (_) => new AlertDialog(
              title: new Text(
                "Confirm Delete",
                style: Styles.blackBoldLarge,
              ),
              content: Container(
                height: 20.0,
                child: Column(
                  children: <Widget>[
                    Text(
                      widget.land == null ? '' : widget.land.name,
                      style: Styles.blackSmall,
                    ),
                  ],
                ),
              ),
              actions: <Widget>[
                FlatButton(
                  child: Text(
                    'NO',
                    style: TextStyle(color: Colors.grey),
                  ),
                  onPressed: () {
                    Navigator.pop(context);
                  },
                ),
                Padding(
                  padding: const EdgeInsets.only(bottom: 20.0),
                  child: RaisedButton(
                    onPressed: () {
                      print('🍏 onPressed to remove all points');
                      Navigator.pop(context);
                      widget.land.polygon.clear();
                      _markersForMap.clear();
                      polygons.clear();

                      setState(() {});
                    },
                    elevation: 4.0,
                    color: Colors.blue.shade700,
                    child: Padding(
                      padding: const EdgeInsets.all(16.0),
                      child: Text(
                        'Remove Point',
                        style: TextStyle(color: Colors.white),
                      ),
                    ),
                  ),
                ),
              ],
            ));
  }

  void _confirmSave() {
    print(
        '🥏 _confirmSave: land parcel 🥏 🥏 ${widget.land.toJson()} ... 🥏 🥏');
    print(
        '🥏 _confirmSave: land parcel coordinate pairs: 🥏 🥏 ${widget.land.polygon.length} ... 🥏 🥏');
    showDialog(
        context: context,
        builder: (context) {
          return AlertDialog(
            title: Text('Land Parcel Confirmation'),
            content: Container(
              height: 180,
              child: Column(
                children: <Widget>[
                  Text(
                      'Please confirm the recording of this Land Parcel. You will be notified when the transaction is sucessful.'),
                  SizedBox(
                    height: 20,
                  ),
                  Text(
                    getFormattedAmount('${widget.land.originalValue}', context),
                    style: Styles.blackBoldMedium,
                  ),
                ],
              ),
            ),
            actions: <Widget>[
              FlatButton(
                onPressed: () {
                  Navigator.pop(context);
                },
                child: Text(
                  'Cancel',
                  style: Styles.blueBoldSmall,
                ),
              ),
              FlatButton(
                onPressed: () {
                  Navigator.pop(context);
                  _submitLandParcel();
                },
                child: Text(
                  'CONFIRM',
                  style: Styles.pinkBoldMedium,
                ),
              ),
            ],
          );
        });
  }

  bool isSubmitting = false;

  void _submitLandParcel() async {
    setState(() {
      isSubmitting = true;
    });
    AppSnackbar.showSnackbarWithProgressIndicator(
        scaffoldKey: _key,
        message: "Submitting Land Parcel",
        textColor: Colors.lime,
        backgroundColor: Colors.brown);
    try {
      print('❤️ Submitting land parcel ....');
      LandDTO result = await Net.startLandRegistrationFlow(widget.land);
      print('🧡 💛 💚 💙 💜 ${result.toJson()}');
      _key.currentState.removeCurrentSnackBar();
      Navigator.push(
          context,
          SlideRightRoute(
            widget: MapViewer(result),
          ));
    } catch (e) {
      setState(() {
        isSubmitting = false;
      });
      AppSnackbar.showErrorSnackbar(
          scaffoldKey: _key, message: e.message, actionLabel: 'Err');
    }
  }
}
