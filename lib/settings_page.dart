import 'package:flutter/material.dart';
import 'package:minimalbitcoinwidget/reusable_widgets/header_list_tile.dart';
import 'package:minimalbitcoinwidget/reusable_widgets/line_divider.dart';

class SettingsPage extends StatelessWidget {
  const SettingsPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        automaticallyImplyLeading: true,
        title: const Text('Settings'),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.start,
          children: [
            const HeaderListTile(title: 'CURRENCY OPTIONS'),
            ListTile(
              title: const Text('U.S. Dollar (\$)'),
              subtitle: const Text('Change Currency'),
              onTap: () {
                print('Change currency button pressed!');
              },
            ),
            const LineDivider(),
            const ListTile(
              title: Text('Version 1.0.0'),
              subtitle: Text('App Version'),
            ),
          ],
        ),
      ),
    );
  }
}
