class NodeInfo {
  List<String> addresses;
  int platformVersion;
  int serial;
  String webAPIUrl;

  NodeInfo(this.addresses, this.platformVersion, this.serial, this.webAPIUrl);

  NodeInfo.fromJson(Map data) {
    List list = data['addresses'];
    this.addresses = List();
    list.forEach((a) {
      this.addresses.add(a as String);
    });
    this.platformVersion = data['platformVersion'];
    this.serial = data['serial'];
    this.webAPIUrl = data['webAPIUrl'];
  }

  Map<String, dynamic> toJson() => <String, dynamic>{
        'addresses': addresses,
        'platformVersion': platformVersion,
        'serial': serial,
        'webAPIUrl': webAPIUrl,
      };
}
