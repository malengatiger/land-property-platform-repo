import 'dart:html';

import 'package:admin/ui/map_viewer.dart';
import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:liplibrary/bloc.dart';
import 'package:liplibrary/data/land.dart';
import 'package:liplibrary/util/functions.dart';
import 'package:liplibrary/util/slide_right.dart';
import 'package:liplibrary/util/theme_bloc.dart';
import 'package:liplibrary/util/znetwork.dart';

import 'land_editor.dart';

class LandList extends StatefulWidget {
  @override
  _LandListState createState() => _LandListState();
}

class _LandListState extends State<LandList> {
  var _key = GlobalKey<ScaffoldState>();
  FirebaseMessaging _firebaseMessaging = FirebaseMessaging();

  var lands = List<LandDTO>();
  @override
  void initState() {
    super.initState();
    _firebaseCloudMessaging();
    _getLandStates();
  }

  void _firebaseCloudMessaging() {
    print(
        '🍊 🍊 _firebaseCloudMessaging started. 🍊 Configuring messaging 🍊 🍊 🍊');

    _firebaseMessaging.getToken().then((token) {
      print("FCM user token :: $token");
    });

    _firebaseMessaging.configure(
      onMessage: (Map<String, dynamic> message) async {
        print('🧩🧩🧩🧩🧩🧩 on message $message');
        var data = message['data'];
        if (data['landState'] != null) {
          print(data);
        }
      },
      onResume: (Map<String, dynamic> message) async {
        print('🧩🧩🧩🧩🧩🧩 on resume $message');
      },
      onLaunch: (Map<String, dynamic> message) async {
        print('🧩🧩🧩🧩🧩🧩 on launch $message');
      },
    );
    _subscribe();
  }

  void _subscribe() {
    _firebaseMessaging.subscribeToTopic('landStates');
    print('🧩🧩🧩🧩🧩🧩 subscribed to FCM topics 🍊 landStates 🍊 ');
  }

  void iOS_Permission() {
    _firebaseMessaging.requestNotificationPermissions(
        IosNotificationSettings(sound: true, badge: true, alert: true));
    _firebaseMessaging.onIosSettingsRegistered
        .listen((IosNotificationSettings settings) {
      print("Settings registered: $settings");
    });
  }

  _getLandStates() async {
    try {
      lands = await Net.getFirestoreParcels();
    } catch (e) {}
    if (lands.isEmpty) {
      lands = await Net.getLandList();
    }

    setState(() {});
  }

  _startLandEditor({LandDTO land}) {
    Navigator.push(
        context,
        SlideRightRoute(
          widget: LandEditor(),
        ));
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      key: _key,
      appBar: AppBar(
        title: Text('Land Parcels List'),
        leading: IconButton(
          icon: Icon(Icons.apps),
          onPressed: () {
            themeBloc.changeToRandomTheme();
          },
        ),
        actions: <Widget>[
          IconButton(
            icon: Icon(Icons.add),
            onPressed: _startLandEditor,
          ),
          IconButton(
            icon: Icon(Icons.refresh),
            onPressed: _getLandStates,
          ),
        ],
        bottom: PreferredSize(
            child: Padding(
              padding: const EdgeInsets.all(8.0),
              child: Padding(
                padding: const EdgeInsets.only(left: 20, right: 20, top: 8),
                child: Column(
                  children: <Widget>[
                    lands.isEmpty
                        ? Container()
                        : Row(
                            mainAxisAlignment: MainAxisAlignment.center,
                            children: <Widget>[
                              Text('Land Records found:'),
                              SizedBox(
                                width: 12,
                              ),
                              Text(
                                '${lands.length}',
                                style: Styles.blackBoldMedium,
                              ),
                            ],
                          ),
                    SizedBox(
                      height: 20,
                    ),
                  ],
                ),
              ),
            ),
            preferredSize: Size.fromHeight(100)),
      ),
      backgroundColor: Colors.brown[50],
      body: StreamBuilder<List<LandDTO>>(
          stream: bloc.landStream,
          builder: (context, snapshot) {
            if (snapshot.hasData) {
              lands = snapshot.data;
            }
            return ListView.builder(
                itemCount: lands.length,
                itemBuilder: (context, index) {
                  var land = lands.elementAt(index);
                  return Stack(
                    children: <Widget>[
                      GestureDetector(
                        onTap: () {
                          _viewLandMap(land);
                        },
                        child: Padding(
                          padding: const EdgeInsets.only(
                              left: 20, right: 20, top: 8),
                          child: Card(
                            elevation: 2,
                            child: Padding(
                              padding: const EdgeInsets.all(16.0),
                              child: Column(
                                children: <Widget>[
                                  Row(
                                    children: <Widget>[
                                      Container(
                                        width: 80,
                                        child: Text(
                                          'Name',
                                          style: Styles.greyLabelSmall,
                                        ),
                                      ),
                                      Text(
                                        land.name,
                                        style: Styles.blackBoldMedium,
                                      ),
                                    ],
                                  ),
                                  SizedBox(
                                    height: 20,
                                  ),
                                  Row(
                                    children: <Widget>[
                                      Container(
                                        width: 80,
                                        child: Text(
                                          'Original Value',
                                          style: Styles.greyLabelSmall,
                                        ),
                                      ),
                                      Text(
                                        getFormattedAmount(
                                            '${land.originalValue}', context),
                                        style: Styles.blackBoldMedium,
                                      ),
                                    ],
                                  ),
                                  SizedBox(
                                    height: 8,
                                  ),
                                  Row(
                                    children: <Widget>[
                                      Container(
                                        width: 80,
                                        child: Text(
                                          'Date',
                                          style: Styles.greyLabelSmall,
                                        ),
                                      ),
                                      Text(
                                        getFormattedDateShortWithTime(
                                            land.dateRegistered, context),
                                        style: Styles.blueSmall,
                                      ),
                                    ],
                                  ),
                                  SizedBox(
                                    height: 20,
                                  ),
                                ],
                              ),
                            ),
                          ),
                        ),
                      ),
                    ],
                  );
                });
          }),
    );
  }

  void _viewLandMap(LandDTO land) {
    Navigator.push(
        context,
        SlideRightRoute(
          widget: MapViewer(land),
        ));
  }
}
