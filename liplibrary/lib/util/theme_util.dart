import 'dart:math';

import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';

class ThemeChanger with ChangeNotifier {
  ThemeData _themeData;

  ThemeChanger(this._themeData);
  getTheme() => _themeData;
  setTheme(ThemeData data) {
    _themeData = data;
    notifyListeners();
  }

  static List<ThemeData> _themes = List();
  static Random random = Random(DateTime.now().millisecondsSinceEpoch);

  static ThemeData getThemeByIndex(int index) {
    print("🧩 🧩 🧩 🧩 🧩 Getting theme index: $index");
    if (_themes.isEmpty) {
      _buildThemes();
    }
    var m = _themes.elementAt(index);
    return m;
  }

  static int getRandomIndex() {
    if (_themes.isEmpty) {
      _buildThemes();
    }
    int index = random.nextInt(_themes.length - 1);
    return index;
  }

  static _buildThemes() {
    _themes.add(ThemeData(
      primaryColor: Colors.teal,
      accentColor: Colors.orange,
      backgroundColor: Colors.brown[100],
    ));
    _themes.add(ThemeData(
      primaryColor: Colors.pink,
      accentColor: Colors.blue,
      backgroundColor: Colors.brown[100],
    ));
    _themes.add(ThemeData(
      primaryColor: Colors.purple,
      accentColor: Colors.orange,
      backgroundColor: Colors.brown[100],
    ));
    _themes.add(ThemeData(
      primaryColor: Colors.indigo,
      accentColor: Colors.lime,
      backgroundColor: Colors.brown[100],
    ));
    _themes.add(ThemeData(
      primaryColor: Colors.blue,
      accentColor: Colors.amber,
      backgroundColor: Colors.brown[100],
    ));
    _themes.add(ThemeData(
      primaryColor: Colors.cyan,
      accentColor: Colors.indigo,
      backgroundColor: Colors.brown[100],
    ));
    _themes.add(ThemeData(
      primaryColor: Colors.cyan,
      accentColor: Colors.orange,
      backgroundColor: Colors.brown[100],
    ));
    _themes.add(ThemeData(
      primaryColor: Colors.orange,
      accentColor: Colors.lime,
      backgroundColor: Colors.brown[100],
    ));
    _themes.add(ThemeData(
      primaryColor: Colors.red,
      accentColor: Colors.blue,
      backgroundColor: Colors.brown[100],
    ));
    _themes.add(ThemeData(
      primaryColor: Colors.blueGrey,
      accentColor: Colors.pink,
      backgroundColor: Colors.brown[100],
    ));
  }
}
