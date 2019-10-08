import 'package:liplibrary/data/coords.dart';

class LandDTO {
  List<String> imageURLs;
  List<CoordinatesDTO> polygon;
  String name;
  double originalValue;
  String dateRegistered;

  LandDTO(
      {this.imageURLs,
      this.polygon,
      this.name,
      this.originalValue,
      this.dateRegistered});

  LandDTO.fromJson(Map data) {
    List list = data['imageURLs'];
    this.imageURLs = List();
    if (list != null) {
      list.forEach((a) {
        this.imageURLs.add(a as String);
      });
    }

    List polygonList = data['polygon'];
    this.polygon = List();
    if (polygonList != null) {
      polygonList.forEach((a) {
        this.polygon.add(CoordinatesDTO(
            latitude: a['latitude'],
            longitude: a['longitude'],
            dateTime: a['dateTime']));
      });
    }
    this.name = data['name'];
    this.originalValue = data['originalValue'];
    this.dateRegistered = data['dateRegistered'];
  }

  Map<String, dynamic> toJson() {
    var map = Map<String, dynamic>();

    map["name"] = name;
    map["originalValue"] = originalValue;
    map["dateRegistered"] = dateRegistered;
    map["imageURLs"] = imageURLs;
    var p = List<Map>();
    if (polygon != null) {
      for (var value in polygon) {
        p.add(value.toJson());
      }
    }
    map['polygon'] = p;

    return map;
  }
}
