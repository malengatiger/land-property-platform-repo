import 'dart:convert';

import 'package:liplibrary/data/account.dart';
import 'package:liplibrary/data/node_info.dart';
import 'package:shared_preferences/shared_preferences.dart';

class Prefs {
  static Future saveAccount(AccountInfo account) async {
    SharedPreferences prefs = await SharedPreferences.getInstance();

    Map map = account.toJson();
    var jx = json.encode(map);
    prefs.setString('account', jx);
    print("🌽 🌽 🌽 Account:  SAVED: 🌽: $jx ");
    return null;
  }

  static Future<AccountInfo> getAccount() async {
    var prefs = await SharedPreferences.getInstance();
    var string = prefs.getString('account');
    if (string == null) {
      return null;
    }
    var jx = json.decode(string);
    var association = new AccountInfo.fromJson(jx);
    print("🌽 🌽 🌽 Account: retrieved : 🧩 🧩 🧩 🧩 🧩 $jx");
    return association;
  }

  static Future saveNode(NodeInfo node) async {
    SharedPreferences prefs = await SharedPreferences.getInstance();

    Map map = node.toJson();
    var jx = json.encode(map);
    prefs.setString('node', jx);
    print("🌽 🌽 🌽 Node:  SAVED: 🌽🧩 🧩 🧩 🧩 : $jx ");
    return null;
  }

  static Future<NodeInfo> getNode() async {
    var prefs = await SharedPreferences.getInstance();
    var string = prefs.getString('node');
    if (string == null) {
      return null;
    }
    var jx = json.decode(string);
    var association = new NodeInfo.fromJson(jx);
    print("🌽 🌽 🌽 Node: retrieved : 🧩 🧩 🧩 🧩 🧩 $jx");
    return association;
  }

  static Future saveNodes(List<NodeInfo> nodes) async {
    SharedPreferences prefs = await SharedPreferences.getInstance();

    var list = List<String>();
    nodes.forEach((node) {
      list.add(json.encode(node));
    });
    prefs.setStringList('nodes', list);
    print("🌽 🌽 🌽  🧩  🧩  🧩 nodes:  SAVED: 🌽: $list ");
    return null;
  }

  static Future<List<NodeInfo>> getNodes() async {
    var prefs = await SharedPreferences.getInstance();
    var strings = prefs.getStringList('nodes');
    var list = List<NodeInfo>();
    if (strings == null) {
      return null;
    }
    strings.forEach((s) {
      list.add(NodeInfo.fromJson(json.decode(s)));
    });

    print("🌽 🌽 🌽  🧩  🧩  🧩  🧩 nodes: retrieved : 🧩 ${list.length}");
    return list;
  }

  static const PATH = '/prefs';
  static void setDemoString(String isDemo) async {
    final preferences = await SharedPreferences.getInstance();
    await preferences.setString('boolKey', isDemo);
    print('🔵 🔵 🔵 demo string set to: $isDemo 🍎 🍎 ');
  }

  static Future<String> getDemoString() async {
    final preferences = await SharedPreferences.getInstance();
    var b = preferences.getString('boolKey');
    if (b == null) {
      return null;
    } else {
      print('🔵 🔵 🔵  demo string retrieved: $b 🍏 🍏 ');
      return b;
    }
  }

  static void setUrl(String url) async {
    final preferences = await SharedPreferences.getInstance();
    await preferences.setString('url', url);
    print('🔵 🔵 🔵 url string set to: $url 🍎 🍎 ');
  }

  static Future<String> getUrl() async {
    final preferences = await SharedPreferences.getInstance();
    var b = preferences.getString('url');
    if (b == null) {
      return null;
    } else {
      print('🔵 🔵 🔵  url string retrieved: $b 🍏 🍏 ');
      return b;
    }
  }

  static void setThemeIndex(int index) async {
    final preferences = await SharedPreferences.getInstance();
    await preferences.setInt('index', index);
    print('🔵 🔵 🔵 Prefs: theme index set to: $index 🍎 🍎 ');
  }

  static Future<int> getThemeIndex() async {
    final preferences = await SharedPreferences.getInstance();
    var b = preferences.getInt('index');
    if (b == null) {
      return 0;
    } else {
      print('🔵 🔵 🔵  theme index retrieved: $b 🍏 🍏 ');
      return b;
    }
  }
}
