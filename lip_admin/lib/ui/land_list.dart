import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:lip_admin/ui/land_editor.dart';
import 'package:lip_admin/ui/map_editor.dart';
import 'package:liplibrary/data/land.dart';
import 'package:liplibrary/util/functions.dart';
import 'package:liplibrary/util/slide_right.dart';
import 'package:liplibrary/util/znetwork.dart';

class LandList extends StatefulWidget {
  @override
  _LandListState createState() => _LandListState();
}

class _LandListState extends State<LandList> {
  var _key = GlobalKey<ScaffoldState>();

  var lands = List<LandDTO>();
  @override
  void initState() {
    super.initState();
    _getLandStates();
  }

  _getLandStates() async {
    lands = await Net.getLandList();
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
        actions: <Widget>[
          IconButton(
            icon: Icon(Icons.add),
            onPressed: _startLandEditor,
          ),
        ],
        bottom: PreferredSize(
            child: Padding(
              padding: const EdgeInsets.all(16.0),
              child: Padding(
                padding: const EdgeInsets.only(left: 20, right: 20, top: 8),
                child: Column(
                  children: <Widget>[
                    lands.isEmpty
                        ? Container()
                        : Row(
                            children: <Widget>[
                              Text('Records found:'),
                              SizedBox(
                                width: 12,
                              ),
                              Text('${lands.length}'),
                            ],
                          ),
                    SizedBox(
                      height: 20,
                    ),
                  ],
                ),
              ),
            ),
            preferredSize: Size.fromHeight(60)),
      ),
      backgroundColor: Colors.brown[50],
      body: ListView.builder(
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
                    padding: const EdgeInsets.only(left: 20, right: 20, top: 8),
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
                                  child: Text('Original Value'),
                                ),
                                Text(
                                  getFormattedAmount(
                                      '${land.originalValue}', context),
                                  style: Styles.blackBoldLarge,
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
          }),
    );
  }

  void _viewLandMap(LandDTO land) {
    Navigator.push(
        context,
        SlideRightRoute(
          widget: MapEditor(land),
        ));
  }
}
