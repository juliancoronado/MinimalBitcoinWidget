import 'package:flutter/material.dart';
import 'package:minimalbitcoinwidget/reusable_widgets/header_list_tile.dart';
import 'package:minimalbitcoinwidget/reusable_widgets/line_divider.dart';
import 'package:url_launcher/url_launcher.dart';

class SettingsPage extends StatelessWidget {
  const SettingsPage({super.key});

  Future<void> openUrl({required String url}) async {
    await launchUrl(
      Uri.parse(url),
      mode: LaunchMode.externalApplication,
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        automaticallyImplyLeading: true,
        title: const Text(
          'Settings',
          style: TextStyle(fontWeight: FontWeight.w600),
        ),
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
                // TODO - implement dialog options
                print('Change currency button pressed!');
              },
            ),
            const LineDivider(),
            const HeaderListTile(title: 'APP INFORMATION'),
            ListTile(
              title: const Text('jcoronado.dev'),
              subtitle: const Text('Developer'),
              trailing: const Icon(Icons.open_in_new),
              onTap: () => openUrl(url: 'https://jcoronado.dev/'),
            ),
            ListTile(
              title: const Text('GitHub Repository'),
              subtitle: const Text('Source Code'),
              trailing: const Icon(Icons.open_in_new),
              onTap: () => openUrl(
                  url:
                      'https://github.com/juliancoronado/MinimalBitcoinWidget/'),
            ),
            const ListTile(
              title: Text('v1.0.0'),
              subtitle: Text('App Version'),
            ),
          ],
        ),
      ),
    );
  }
}
