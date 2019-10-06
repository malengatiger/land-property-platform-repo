class CoordinatesDTO {
  double latitude, longitude;

  CoordinatesDTO(this.latitude, this.longitude);

  CoordinatesDTO.fromJson(Map data) {
    this.latitude = data['latitude'];
    this.longitude = data['longitude'];
  }

  Map<String, dynamic> toJson() => <String, dynamic>{
        'latitude': latitude,
        'longitude': longitude,
      };
}
