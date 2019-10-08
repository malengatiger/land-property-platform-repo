import 'package:admin/ui/land_list.dart';
import 'package:flutter/material.dart';
import 'package:liplibrary/util/theme_bloc.dart';
import 'package:liplibrary/util/zprefs.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  // This widget is the root of your application.
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  int themeIndex;

  void _getTheme() async {
    themeIndex = await Prefs.getThemeIndex();
    setState(() {});
  }

  @override
  void initState() {
    super.initState();
    _getTheme();
  }

  @override
  Widget build(BuildContext context) {
    return StreamBuilder<int>(
        stream: themeBloc.newThemeStream,
        builder: (context, snapshot) {
          if (snapshot.hasData) {
            themeIndex = snapshot.data;
          }
          return MaterialApp(
            title: 'Flutter Demo',
            debugShowCheckedModeBanner: false,
            theme: ThemeUtil.getTheme(themeIndex),
            home: LandList(),
          );
        });
  }
}
