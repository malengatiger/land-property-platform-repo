import 'package:admin/ui/land_list.dart';
import 'package:flutter/material.dart';
import 'package:liplibrary/util/theme_bloc.dart';

void main() => runApp(MyApp());

class MyApp extends StatelessWidget {
  // This widget is the root of your application.
  var themeBloc = ThemeBloc();
  var index = 0;
  @override
  Widget build(BuildContext context) {
    return StreamBuilder<int>(
        stream: themeBloc.newThemeStream,
        builder: (context, snapshot) {
          if (snapshot.hasData) {
            index = snapshot.data;
          }
          return MaterialApp(
            title: 'Flutter Demo',
            debugShowCheckedModeBanner: false,
            theme: ThemeUtil.getTheme(index),
            home: LandList(),
          );
        });
  }
}
