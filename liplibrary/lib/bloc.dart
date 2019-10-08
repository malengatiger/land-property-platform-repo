import 'dart:async';

import 'package:firebase_auth/firebase_auth.dart';
import 'package:liplibrary/data/land.dart';
import 'package:liplibrary/util/znetwork.dart';
import 'package:location/location.dart';
import 'package:permission_handler/permission_handler.dart';

import 'data/position.dart';

Bloc bloc = Bloc();

class Bloc {
  FirebaseAuth auth = FirebaseAuth.instance;
  FirebaseUser _user;

  StreamController<List<LandDTO>> _landController =
      StreamController.broadcast();
  Stream get landStream => _landController.stream;
  void closeStreams() {
    _landController.close();
  }

  Future<List<LandDTO>> getLandStates() async {
    var list = await Net.getLandList();
    if (list.isEmpty) {
      list = await Net.getFirestoreParcels();
    }
    _landController.sink.add(list);
    return list;
  }

  Future<FirebaseUser> signIn(String email, String password) async {
    var result =
        await auth.signInWithEmailAndPassword(email: email, password: password);
    if (result.user == null) {
      throw Exception('User sigin failed');
    }
    print('🥬 🥬 🥬 User successfully signed in: 🍎 🍊 ${result.user.uid}');
    _user = result.user;
    return _user;
  }

  Future<Position> getCurrentLocation() async {
    await checkPermission();
    var location = Location();
    try {
      var mLocation = await location.getLocation();
      return Position.fromJson({
        'coordinates': [mLocation.longitude, mLocation.latitude],
        'type': 'Point',
      });
    } catch (e) {
      throw Exception('Permission denied');
    }
  }

  Future checkPermission() async {
    print(' 🔆 🔆 🔆 🔆 checking permission ...');

    final Future<PermissionStatus> statusFuture =
        PermissionHandler().checkPermissionStatus(PermissionGroup.location);

    statusFuture.then((PermissionStatus status) {
      switch (status) {
        case PermissionStatus.granted:
          print('location is GRANTED:  ❤️ 🧡 💛 💚 💙 💜 ....');
          break;
        case PermissionStatus.denied:
          print('location is DENIED 🔱 🔱 🔱 🔱 🔱 ');
          requestPermission();
          break;
        case PermissionStatus.disabled:
          print('location is DiSABLED  🔕 🔕 🔕 🔕 🔕 ');
          requestPermission();
          break;
        case PermissionStatus.unknown:
          print('location is UNKNOWN  🔕 🔕 🔕 🔕 🔕 ');
          requestPermission();
          break;
      }
    });
  }

  Future requestPermission() async {
    print('🧩🧩🧩🧩 Requesting permission ....  🧩🧩🧩🧩');
    PermissionStatus permission = await PermissionHandler()
        .checkPermissionStatus(PermissionGroup.location);
    final List<PermissionGroup> permissions = <PermissionGroup>[
      PermissionGroup.location
    ];
    final Map<PermissionGroup, PermissionStatus> permissionRequestResult =
        await PermissionHandler().requestPermissions(permissions);

    var permissionStatus = permissionRequestResult[permission];
    if (permissionStatus == PermissionStatus.granted) {
      print('💚💚💚💚 Permission has been  granted. 🍏🍏 Yeah!');
    }
  }

  Bloc() {
    print('🧩🧩🧩🧩 Requesting constructor ....  🧩🧩🧩🧩');
  }
}
