import 'dart:convert';

import 'package:cloud_firestore/cloud_firestore.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'package:liplibrary/data/account.dart';
import 'package:liplibrary/data/land.dart';
import 'package:liplibrary/data/node_info.dart';
import 'package:liplibrary/util/zprefs.dart';

class Net {
  static Firestore db = Firestore.instance;
  static FirebaseAuth auth = FirebaseAuth.instance;
  static const URL = 'http://192.168.86.240:10095/';
  static const Map<String, String> headers = {
    'Content-type': 'application/json',
    'Accept': 'application/json',
  };

  static Future _getCachedURL() async {
    var url = await Prefs.getUrl();
    return url;
  }

  static Future getUser(String email) async {
    String url = await _getCachedURL();

    return await get(url + 'admin/getUser');
  }

  static Future<List<NodeInfo>> getNodes() async {
    var snapshot = await db.collection("nodes").getDocuments();
    print('nodes found on network: ${snapshot.documents.length}');
    List<NodeInfo> list = List();
    snapshot.documents.forEach((doc) {
      var data = doc.data;
      print('data from Firestore: $data');
      var node = NodeInfo.fromJson(data);
      list.add(node);
    });
    return list;
  }

  static Future<String> get(String mUrl) async {
    var start = DateTime.now();
    var client = new http.Client();
    var resp = await client
        .get(
      mUrl,
      headers: headers,
    )
        .whenComplete(() {
      client.close();
    });

    var end = DateTime.now();
    debugPrint(
        '🍎 🍊 Net: post  ##################### elapsed: ${end.difference(start).inSeconds} seconds\n\n');
    if (resp.statusCode == 200) {
      debugPrint(
          '🍎 🍊 Net: get: SUCCESS: Network Response Status Code: 🥬  🥬 ${resp.statusCode} 🥬  $mUrl');
      return resp.body;
    } else {
      var msg = ' 👿  Failed status code: ${resp.statusCode} 🥬  $mUrl';
      debugPrint(msg);
      throw Exception(msg);
    }
  }

  static Future post(String mUrl, Map bag) async {
    var start = DateTime.now();
    var client = new http.Client();
    String body;
    if (bag != null) {
      body = json.encode(bag);
    }
    debugPrint('🍊 🍊 🍊 Net: post ... calling with bag: $body');
    var resp = await client
        .post(
      mUrl,
      body: body,
      headers: headers,
    )
        .whenComplete(() {
      debugPrint('🍊 🍊 🍊 Net: post whenComplete ');
      client.close();
    });
    print(resp.body);
    var end = DateTime.now();
    debugPrint(
        '🍎 🍊 Net: post  ##################### elapsed: ${end.difference(start).inSeconds} seconds\n\n');
    if (resp.statusCode == 200) {
      debugPrint(
          '🍎 🍊 Net: post: SUCCESS: Network Response Status Code: 🥬  🥬 ${resp.statusCode} 🥬  $mUrl');
      return resp.body;
    } else {
      var msg = ' 👿  Failed status code: ${resp.statusCode} 🥬  $mUrl';
      debugPrint(resp.body);
      throw Exception(msg);
    }
  }

  static Future<LandDTO> startLandRegistrationFlow(LandDTO landDTO) async {
    var bag = landDTO.toJson();

    debugPrint(
        '🍊 🍊 🍊 🍊 🍊 startLandRegistrationFlow starting the call ...');
    final response = await post(URL + 'land/startLandRegistrationFlow', bag);
    var m = json.decode(response);
    var land = LandDTO.fromJson(m);
    try {
      await addLandToFirestore(land);
    } catch (e) {
      print(' 🔆🔆🔆🔆 Firestore call failed. we might not be online');
    }
    return land;
  }

  static Future addLandToFirestore(LandDTO land) async {
    var ref = await db.collection('landParcels').add(land.toJson());
    print('🥬  🥬 land parcel has been cached to Firestore: ${ref.path}');
  }

  static Future<List<LandDTO>> getFirestoreParcels() async {
    print('🥬  🥬 getFirestoreParcels ...');
    List<LandDTO> list = List();
    QuerySnapshot res = await db.collection('landParcels').getDocuments();
    res.documents.forEach((doc) {
      var m = LandDTO.fromJson(doc.data);
      list.add(m);
    });
    print('🥬  🥬 getFirestoreParcels ... found: ${list.length}');
    return list;
  }

  static Future<List<LandDTO>> restore() async {
    List<LandDTO> list = await getFirestoreParcels();
    List<LandDTO> list2 = List();
    for (var x in list) {
      var d = await startLandRegistrationFlow(x);
      list2.add(d);
      print('🔵 🔵 🔵 Land restored: 🔵 🔵 🔵  ${d.toJson()}');
    }
    return list2;
  }

  static Future<List<AccountInfo>> getAccounts() async {
    var prefix; //await getNodeUrl();
    final response = await get(prefix + 'admin/getAccounts');

    List<AccountInfo> list = List();
    List m = json.decode(response);
    m.forEach((f) {
      list.add(AccountInfo.fromJson(f));
    });
    debugPrint('🍎 🍊 Net: getAccounts: found ${list.length}');
    return list;
  }

  static Future<List<LandDTO>> getLandList() async {
    final response = await get(URL + 'land/listLandStates');

    List<LandDTO> list = List();
    List m = json.decode(response);
    m.forEach((f) {
      list.add(LandDTO.fromJson(f));
    });
    debugPrint('🍎 🍊 Net: getLandList: found ${list.length}');
    return list;
  }

  static Future<AccountInfo> getAccount(String accountId) async {
    var node = await Prefs.getNode();
    final response =
        await get(node.webAPIUrl + 'admin/getAccount?accountId=$accountId');

    AccountInfo acctInfo = AccountInfo.fromJson(json.decode(response));
    debugPrint('🍎 🍊 Net: getAccount: found ${acctInfo.toJson()}');
    return acctInfo;
  }

  static Future startAccountRegistrationFlow() async {}

  static Future<String> ping() async {
//    var node = await Prefs.getNode();
//    final response = await http.get(node.webAPIUrl + 'admin/ping');
//    if (response.statusCode == 200) {
//      debugPrint(
//          '🍎 🍊 Net: ping: Network Response Status Code: 🥬  🥬 ${response.statusCode} 🥬 ');
//      return response.body;
//    } else {
//      throw Exception(' 👿  Failed ping');
//    }
  }
}
