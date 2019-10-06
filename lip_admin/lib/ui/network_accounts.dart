import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:liplibrary/data/account.dart';
import 'package:liplibrary/util/functions.dart';
import 'package:liplibrary/util/znetwork.dart';

class NetworkAccountsPage extends StatefulWidget {
  @override
  _NetworkAccountsPageState createState() => _NetworkAccountsPageState();
}

class _NetworkAccountsPageState extends State<NetworkAccountsPage> {
  var _key = GlobalKey<ScaffoldState>();
  List<AccountInfo> accounts = List(), filteredAccounts = List();
  String filter;
  bool showFilter = true;
  bool showAllAccounts = false;

  @override
  initState() {
    super.initState();
    _getAccounts();
  }

  _getAccounts() async {
    accounts = await Net.getAccounts();
    accounts.sort((a, b) => a.name.compareTo(b.name));
    if (accounts.length < 101) {
      showAllAccounts = true;
      filteredAccounts = accounts;
    }
    setState(() {});
  }

  void _dismissKeyboard() {
    FocusScope.of(context).requestFocus(new FocusNode());
  }

  void _filterAccounts() {
    if (showAllAccounts) {
      setState(() {
        filteredAccounts = accounts;
        filteredAccounts.sort((a, b) => a.name.compareTo(b.name));
      });
      return;
    }
    if (filter.isEmpty) {
      setState(() {
        filteredAccounts.clear();
      });
      return;
    }
    filteredAccounts.clear();
    accounts.forEach((v) {
      if (v.name.toLowerCase().contains(filter.toLowerCase())) {
        filteredAccounts.add(v);
      }
    });
    filteredAccounts.sort((a, b) => a.name.compareTo(b.name));
    setState(() {});
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      key: _key,
      appBar: AppBar(
        title: Text("BFN Accounts"),
        bottom: _getBottom(),
        actions: <Widget>[
          IconButton(
            icon: Icon(Icons.refresh),
            onPressed: _getAccounts,
          )
        ],
      ),
      backgroundColor: Colors.brown[100],
      body: ListView.builder(
          itemCount: filteredAccounts.length,
          itemBuilder: (BuildContext context, int index) {
            return Padding(
              padding: const EdgeInsets.only(left: 8.0, right: 8, top: 8),
              child: Card(
                elevation: 4,
                child: ListTile(
                  leading: Icon(
                    Icons.account_circle,
                    color: Colors.pink,
                  ),
                  title: Text(
                    filteredAccounts.elementAt(index).name,
                    style: Styles.blackBoldMedium,
                  ),
                  subtitle: Text(filteredAccounts.elementAt(index).host),
                  onTap: () {
                    print(
                        '🍎 🍊 selected account ${filteredAccounts.elementAt(index).toJson()}');
                    Navigator.pop(context, filteredAccounts.elementAt(index));
                  },
                ),
              ),
            );
          }),
    );
  }

  Widget _getBottom() {
    return PreferredSize(
        child: showAllAccounts
            ? Column()
            : Column(
                children: <Widget>[
                  Padding(
                    padding: const EdgeInsets.only(left: 20, right: 20),
                    child: Card(
                      elevation: 8,
                      child: Padding(
                        padding: const EdgeInsets.only(left: 16.0),
                        child: TextField(
                          style: Styles.blackMedium,
                          decoration: InputDecoration(
                              suffix: IconButton(
                                icon: Icon(
                                  Icons.close,
                                  color: Colors.pink,
                                ),
                                onPressed: _dismissKeyboard,
                              ),
                              hintText: 'Find network account '),
                          onChanged: (val) {
                            filter = val;
                            _filterAccounts();
                          },
                        ),
                      ),
                    ),
                  ),
                  Padding(
                    padding: const EdgeInsets.all(8.0),
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.end,
                      children: <Widget>[
                        Text(
                          'Accounts Found:',
                          style: Styles.whiteMedium,
                        ),
                        SizedBox(
                          width: 8,
                        ),
                        Text(
                          '${filteredAccounts.length}',
                          style: Styles.blackBoldMedium,
                        ),
                        SizedBox(
                          width: 12,
                        ),
                        Text(
                          'of',
                          style: Styles.whiteMedium,
                        ),
                        SizedBox(
                          width: 12,
                        ),
                        Text(
                          '${accounts.length}',
                          style: Styles.whiteBoldMedium,
                        ),
                        SizedBox(
                          width: 20,
                        ),
                      ],
                    ),
                  ),
                  SizedBox(
                    height: 20,
                  )
                ],
              ),
        preferredSize: Size.fromHeight(showAllAccounts ? 40 : 180));
  }
}
