class AccountInfo {
  String identifier;
  String host;
  String name;
  String status;


  AccountInfo(this.identifier, this.host, this.name, this.status);

  AccountInfo.fromJson(Map data) {
    this.identifier = data['identifier'];
    this.host = data['host'];
    this.name = data['name'];
    this.status = data['status'];
  }

  Map<String, dynamic> toJson() => <String, dynamic>{
        'identifier': identifier,
        'host': host,
        'name': name,
        'status': status,
      };
}

