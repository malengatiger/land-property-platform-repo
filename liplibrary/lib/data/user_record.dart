class UserRecord {
  String uid;
  String email;
  String phoneNumber;
  bool emailVerified;
  String displayName;
  Null photoUrl;
  bool disabled;
  int tokensValidAfterTimestamp;
  UserMetadata userMetadata;
  CustomClaims customClaims;
  String providerId;
  List<ProviderData> providerData;

  UserRecord(
      {this.uid,
      this.email,
      this.phoneNumber,
      this.emailVerified,
      this.displayName,
      this.photoUrl,
      this.disabled,
      this.tokensValidAfterTimestamp,
      this.userMetadata,
      this.customClaims,
      this.providerId,
      this.providerData});

  UserRecord.fromJson(Map<String, dynamic> json) {
    uid = json['uid'];
    email = json['email'];
    phoneNumber = json['phoneNumber'];
    emailVerified = json['emailVerified'];
    displayName = json['displayName'];
    photoUrl = json['photoUrl'];
    disabled = json['disabled'];
    tokensValidAfterTimestamp = json['tokensValidAfterTimestamp'];
    userMetadata = json['userMetadata'] != null
        ? new UserMetadata.fromJson(json['userMetadata'])
        : null;
    customClaims = json['customClaims'] != null
        ? new CustomClaims.fromJson(json['customClaims'])
        : null;
    providerId = json['providerId'];
    if (json['providerData'] != null) {
      providerData = new List<ProviderData>();
      json['providerData'].forEach((v) {
        providerData.add(new ProviderData.fromJson(v));
      });
    }
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['uid'] = this.uid;
    data['email'] = this.email;
    data['phoneNumber'] = this.phoneNumber;
    data['emailVerified'] = this.emailVerified;
    data['displayName'] = this.displayName;
    data['photoUrl'] = this.photoUrl;
    data['disabled'] = this.disabled;
    data['tokensValidAfterTimestamp'] = this.tokensValidAfterTimestamp;
    if (this.userMetadata != null) {
      data['userMetadata'] = this.userMetadata.toJson();
    }
    if (this.customClaims != null) {
      data['customClaims'] = this.customClaims.toJson();
    }
    data['providerId'] = this.providerId;
    if (this.providerData != null) {
      data['providerData'] = this.providerData.map((v) => v.toJson()).toList();
    }
    return data;
  }
}

class UserMetadata {
  int creationTimestamp;
  int lastSignInTimestamp;

  UserMetadata({this.creationTimestamp, this.lastSignInTimestamp});

  UserMetadata.fromJson(Map<String, dynamic> json) {
    creationTimestamp = json['creationTimestamp'];
    lastSignInTimestamp = json['lastSignInTimestamp'];
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['creationTimestamp'] = this.creationTimestamp;
    data['lastSignInTimestamp'] = this.lastSignInTimestamp;
    return data;
  }
}

class CustomClaims {
  CustomClaims();

  CustomClaims.fromJson(Map<String, dynamic> json) {}

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    return data;
  }
}

class ProviderData {
  String uid;
  String displayName;
  String email;
  String phoneNumber;
  Null photoUrl;
  String providerId;

  ProviderData(
      {this.uid,
      this.displayName,
      this.email,
      this.phoneNumber,
      this.photoUrl,
      this.providerId});

  ProviderData.fromJson(Map<String, dynamic> json) {
    uid = json['uid'];
    displayName = json['displayName'];
    email = json['email'];
    phoneNumber = json['phoneNumber'];
    photoUrl = json['photoUrl'];
    providerId = json['providerId'];
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['uid'] = this.uid;
    data['displayName'] = this.displayName;
    data['email'] = this.email;
    data['phoneNumber'] = this.phoneNumber;
    data['photoUrl'] = this.photoUrl;
    data['providerId'] = this.providerId;
    return data;
  }
}
