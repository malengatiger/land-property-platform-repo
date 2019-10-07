import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter/material.dart';
import 'package:liplibrary/data/account.dart';
import 'package:liplibrary/data/node_info.dart';
import 'package:liplibrary/data/user_record.dart';
import 'package:liplibrary/util/functions.dart';
import 'package:liplibrary/util/slide_right.dart';
import 'package:liplibrary/util/snack.dart';
import 'package:liplibrary/util/znetwork.dart';
import 'package:liplibrary/util/zprefs.dart';

import 'network_accounts.dart';

class SignUp extends StatefulWidget {
  @override
  _SignUpState createState() => _SignUpState();
}

class _SignUpState extends State<SignUp> {
  final _key = GlobalKey<ScaffoldState>();
  final _nameKey = GlobalKey<FormState>();
  final _emailKey = GlobalKey<FormState>();
  final _cellKey = GlobalKey<FormState>();
  final _passKey = GlobalKey<FormState>();
  final _formKey = GlobalKey<FormState>();

  String name, email, cellphone, password;
  FirebaseAuth auth = FirebaseAuth.instance;
  List<NodeInfo> nodes = List();
  List<DropdownMenuItem<NodeInfo>> items = List();
  @override
  void initState() {
    super.initState();
    _getNodes();
  }

  _getNodes() async {
    nodes = await Net.getNodes();
    nodes.forEach((n) {
      if (n.webAPIUrl != null) {
        print('add to dropdown ${n.webAPIUrl}');
        items.add(DropdownMenuItem(
            value: n,
            child: Text(
              n.addresses.elementAt(0),
              style: Styles.blackBoldSmall,
            )));
      } else {
        print(
            ' 👿  👿  👿 ignore possible notary node - no webAPIUrl available');
      }
    });
    print('..................dropDownItems: ${items.length}');
    setState(() {});
  }

  @override
  Widget build(BuildContext context) {
    return WillPopScope(
      child: Scaffold(
        key: _key,
        appBar: AppBar(
          leading: Icon(Icons.people),
          title: Text('BFN SignUp'),
          actions: <Widget>[
            IconButton(
              icon: Icon(Icons.person_add),
              onPressed: _changeAccount,
            ),
          ],
          bottom: PreferredSize(
              child: Column(
                children: <Widget>[
                  Text(
                    "Business Finance Network",
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
                            "Account Details",
                            style: TextStyle(
                                fontSize: 28, fontWeight: FontWeight.w900),
                          ),
                          SizedBox(
                            height: 8,
                          ),
                          DropdownButton(
                              items: items,
                              hint: Text('Select Network Node'),
                              onChanged: _dropDownChanged),
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
//                              if (value.isEmpty) {
//                                return 'Please enter name';
//                              }
                              name = value;
                              return null;
                            },
                          ),
                          TextFormField(
                            key: _emailKey,
                            decoration: InputDecoration(
                                icon: Icon(Icons.email),
                                hintText: "Enter Email Address",
                                labelText: "Email"),
                            keyboardType: TextInputType.emailAddress,
                            // The validator receives the text that the user has entered.
                            validator: (value) {
                              if (value.isEmpty) {
                                return 'Please enter email address';
                              }
                              email = value;
                              return null;
                            },
                          ),
                          TextFormField(
                            key: _cellKey,
                            decoration: InputDecoration(
                                icon: Icon(Icons.phone),
                                hintText: "Enter Cellphone Number",
                                labelText: " Cellphone Number"),
                            keyboardType: TextInputType.phone,
                            // The validator receives the text that the user has entered.
                            validator: (value) {
//                              if (value.isEmpty) {
//                                return 'Please enter cellphone number';
//                              }
                              cellphone = value;
                              return null;
                            },
                          ),
                          TextFormField(
                            key: _passKey,
                            decoration: InputDecoration(
                                icon: Icon(Icons.vpn_key),
                                hintText: "Enter Password",
                                labelText: "Password"),
                            keyboardType: TextInputType.visiblePassword,
                            // The validator receives the text that the user has entered.
                            validator: (value) {
                              if (value.isEmpty) {
                                return 'Please enter password';
                              }
                              password = value;
                              return null;
                            },
                          ),
                          SizedBox(
                            height: 28,
                          ),
                          selectedNode == null
                              ? Text("No Network Node")
                              : Row(
                                  children: <Widget>[
                                    Text(
                                      "Node",
                                      style: Styles.greyLabelSmall,
                                    ),
                                    SizedBox(
                                      width: 8,
                                    ),
                                    Text(
                                      selectedNode.addresses.elementAt(0),
                                      style: Styles.tealBoldSmall,
                                    ),
                                  ],
                                ),
                          SizedBox(
                            height: 40,
                          ),
                          SizedBox(height: 0),
                          RaisedButton(
                            color: Colors.indigo,
                            elevation: 8,
                            child: Padding(
                              padding: const EdgeInsets.all(20.0),
                              child: Text(
                                "Create Account",
                                style: TextStyle(color: Colors.white),
                              ),
                            ),
                            onPressed: _validate,
                          )
                        ],
                      ),
                    ),
                  )),
            ),
          ],
        ),
      ),
      onWillPop: () => doNothing(),
    );
  }

  _validate() async {
    if (selectedNode == null) {
      _error('Please select Network Node');
      return;
    }
    if (_formKey.currentState.validate()) {
      print("🍎 🍊 ready to rumble $name $email $cellphone $password");
      UserRecord userRecord;
      try {
        userRecord = await Net.getUser(email);
      } catch (e) {
        print(e);
      }
      AccountInfo accountInfo;
//      try {
//        if (userRecord != null) {
//          AuthResult authResult = await auth.signInWithEmailAndPassword(
//              email: email, password: password);
//          if (authResult.user != null) {
//            //get account info - using uid
//            try {
//              accountInfo = await Net.getAccount(authResult.user.uid);
//            } catch (e) {
//              print(e);
//            }
//          } else {
//            accountInfo = await Net.startAccountRegistrationFlow(
//                name, email, password, cellphone);
//          }
//          //sign IN
//        } else {
//          print('🍎 🍊 🍎 🍊 🍎 🍊 ... creating account flow .....');
//          accountInfo = await Net.startAccountRegistrationFlow(
//              name, email, password, cellphone);
//        }
//        print(
//            '🍎 🍊 🍎 🍊 🍎 🍊 acct found or created: ${accountInfo.toJson()} 🍎 🍊 🍎 🍊 🍎 🍊 ');
//        await Prefs.saveAccount(accountInfo);
//        var result = await bloc.signIn(email, password);
//        print(result);
//
//        Navigator.push(
//            context,
//            SlideRightRoute(
//              widget: Dashboard(),
//            ));
//      } catch (e) {
//        print(e);
//        _error("Account registration failed");
//      }
    }
  }

  _error(String msg) {
    AppSnackbar.showErrorSnackbar(
        scaffoldKey: _key, message: msg, actionLabel: "Err");
  }

  Future<bool> doNothing() async {
    return false;
  }

  void _changeAccount() async {
    var result = await Navigator.push(
        context,
        SlideRightRoute(
          widget: NetworkAccountsPage(),
        ));
    if (result != null) {
      print(result);
      var account = result as AccountInfo;
      await Prefs.saveAccount(account);
      var auth = FirebaseAuth.instance;
      await auth.signInAnonymously();
      nodes.forEach((n) async {
        if (account.name == n.addresses.elementAt(0)) {
          await Prefs.saveNode(n);
        }
      });
      print(
          '🍊 🍊 🍊 🍊 Signed in FRESH (anonymous) to Firebase: ${result.toString()}');
      Navigator.push(
          context,
          SlideRightRoute(
            widget: Dashboard(),
          ));
    }
  }

  NodeInfo selectedNode;
  void _dropDownChanged(NodeInfo value) async {
    selectedNode = value;
    print('🌸 🌸 🌸 Selected node: ${selectedNode.toJson()}');
    setState(() {});
    Prefs.saveNode(selectedNode);
  }
}

class Dashboard extends StatefulWidget {
  @override
  _DashboardState createState() => _DashboardState();
}

class _DashboardState extends State<Dashboard> {
  @override
  Widget build(BuildContext context) {
    return Container();
  }
}
