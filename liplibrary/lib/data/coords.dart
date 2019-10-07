class CoordinatesDTO {
  double latitude, longitude;
  String dateTime;

  CoordinatesDTO({this.latitude, this.longitude, this.dateTime}) {
    if (this.dateTime == null) {
      this.dateTime = DateTime.now().toIso8601String();
    }
  }

  CoordinatesDTO.fromJson(Map data) {
    this.latitude = data['latitude'];
    this.longitude = data['longitude'];
    this.dateTime = data['dateTime'];
  }

  Map<String, dynamic> toJson() => <String, dynamic>{
        'latitude': latitude,
        'longitude': longitude,
        'dateTime': dateTime,
      };
}
