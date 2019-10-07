import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:liplibrary/data/land.dart';
import 'package:liplibrary/util/functions.dart';
import 'package:liplibrary/util/slide_right.dart';

import 'map_editor.dart';

class LandEditor extends StatefulWidget {
  @override
  _LandEditorState createState() => _LandEditorState();
}

class _LandEditorState extends State<LandEditor> {
  var _key = GlobalKey<ScaffoldState>();
  final _nameKey = GlobalKey<FormState>();
  final _cellKey = GlobalKey<FormState>();
  final _formKey = GlobalKey<FormState>();

  @override
  void initState() {
    // TODO: implement initState
    super.initState();
  }

  String name, originalValue;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      key: _key,
      appBar: AppBar(
        leading: Icon(Icons.people),
        title: Text('Land Editor'),
        bottom: PreferredSize(
            child: Column(
              children: <Widget>[
                Text(
                  "Land Finance Network",
                  style: TextStyle(
                      color: Colors.white,
                      fontWeight: FontWeight.w900,
                      fontSize: 24),
                ),
                SizedBox(
                  height: 24,
                )
              ],
            ),
            preferredSize: Size.fromHeight(60)),
      ),
      backgroundColor: Colors.brown[100],
      body: ListView(
        children: <Widget>[
          Padding(
            padding: const EdgeInsets.all(8.0),
            child: Card(
                elevation: 4,
                child: Padding(
                  padding: const EdgeInsets.all(16.0),
                  child: Form(
                    key: _formKey,
                    child: Column(
                      children: <Widget>[
                        Text(
                          "Land Parcel Details",
                          style: TextStyle(
                              fontSize: 28, fontWeight: FontWeight.w900),
                        ),
                        SizedBox(
                          height: 8,
                        ),
                        TextFormField(
                          key: _nameKey,
                          decoration: InputDecoration(
                              icon: Icon(Icons.person),
                              hintText: "Enter Name",
                              labelText: "Name"),
                          keyboardType: TextInputType.text,
                          // The validator receives the text that the user has entered.
                          validator: (value) {
                            if (value.isEmpty) {
                              return 'Please enter name';
                            }
                            name = value;
                            return null;
                          },
                        ),
                        TextFormField(
                          key: _cellKey,
                          decoration: InputDecoration(
                              hintStyle: Styles.blackBoldMedium,
                              icon: Icon(Icons.attach_money),
                              hintText: "Enter Original Value",
                              labelText: " Original Value"),
                          keyboardType:
                              TextInputType.numberWithOptions(decimal: true),
                          validator: (value) {
                            if (value.isEmpty) {
                              return 'Please enter value';
                            }
                            originalValue = value;
                            return null;
                          },
                        ),
                        SizedBox(height: 20),
                        RaisedButton(
                          color: Colors.indigo,
                          elevation: 8,
                          child: Padding(
                            padding: const EdgeInsets.all(20.0),
                            child: Text(
                              "Create Land Record",
                              style: TextStyle(color: Colors.white),
                            ),
                          ),
                          onPressed: _confirm,
                        )
                      ],
                    ),
                  ),
                )),
          ),
        ],
      ),
    );
  }

  void _confirm() async {
    if (_formKey.currentState.validate()) {
      print("🍎 🍊 ready to rumble $name ");
      LandDTO m = LandDTO(
        name: name,
        originalValue: double.parse(originalValue),
        polygon: List(),
        imageURLs: List(),
      );
      Navigator.push(
          context,
          SlideRightRoute(
            widget: MapEditor(m),
          ));
    } else {
      print("🍎 validation has failed ");
    }
  }
}
