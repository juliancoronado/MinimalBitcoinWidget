import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:minimalbitcoinwidget/constants.dart';
import 'package:minimalbitcoinwidget/providers/currency_provider.dart';
import 'package:minimalbitcoinwidget/providers/shared_preferences_provider.dart';
import 'package:minimalbitcoinwidget/reusable_widgets/header_list_tile.dart';
import 'package:minimalbitcoinwidget/reusable_widgets/line_divider.dart';
import 'package:minimalbitcoinwidget/screens/map_01.dart';
import 'package:url_launcher/url_launcher.dart';

class SettingsPage extends ConsumerStatefulWidget {
  const SettingsPage({super.key});

  @override
  ConsumerState createState() => SettingsPageState();
}

class SettingsPageState extends ConsumerState<SettingsPage> {
  Future<void> openUrl({required String url}) async {
    await launchUrl(
      Uri.parse(url),
      mode: LaunchMode.externalApplication,
    );
  }

  int? getKeyFromValue(String value) {
    for (MapEntry<int, String> entry in currencyMapping.entries) {
      if (entry.value == value) {
        return entry.key;
      }
    }
    return null;
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
              title: Text(ref.watch(currencyProvider)),
              subtitle: const Text('Change Currency'),
              onTap: () async {
                int selectedOption =
                    getKeyFromValue(ref.read(currencyProvider)) ?? 0;
                int? savedOption = await showDialog(
                  context: context,
                  builder: (BuildContext context) {
                    return AlertDialog(
                      title: const Text('Change Currency'),
                      content: StatefulBuilder(
                        builder: (context, setState) {
                          return Column(
                            mainAxisSize: MainAxisSize.min,
                            children: currencyMapping.entries.map(
                              (entry) {
                                return RadioListTile(
                                    title: Text(entry.value),
                                    value: entry.key,
                                    groupValue: selectedOption,
                                    onChanged: (value) {
                                      if (value == null) return;
                                      setState(() {
                                        selectedOption = value;
                                      });
                                    });
                              },
                            ).toList(),
                          );
                        },
                      ),
                      actions: [
                        TextButton(
                            onPressed: () => Navigator.of(context).pop(),
                            child: const Text('Cancel')),
                        TextButton(
                            onPressed: () =>
                                Navigator.of(context).pop(selectedOption),
                            child: const Text('Save')),
                      ],
                    );
                  },
                );

                if (savedOption == null) return;

                // setState(() {
                //   selectedCurrency = currencyMapping[savedOption]!;
                // });

                String newValue = currencyMapping[savedOption]!;

                ref.read(currencyProvider.notifier).state = newValue;

                ref
                    .read(sharedPreferencesProvider)
                    .setString(currencyKey, newValue);
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
