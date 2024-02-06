import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:minimalbitcoinwidget/constants.dart';
import 'package:minimalbitcoinwidget/helpers/functions.dart';
import 'package:minimalbitcoinwidget/providers/api_provider.dart';
import 'package:minimalbitcoinwidget/providers/currency_provider.dart';
import 'package:minimalbitcoinwidget/providers/shared_preferences_provider.dart';
import 'package:minimalbitcoinwidget/widgets/header_list_tile.dart';
import 'package:minimalbitcoinwidget/widgets/line_divider.dart';
import 'package:minimalbitcoinwidget/currency_mapping.dart';

class SettingsPage extends ConsumerStatefulWidget {
  const SettingsPage({super.key});

  @override
  ConsumerState createState() => SettingsPageState();
}

class SettingsPageState extends ConsumerState<SettingsPage> {
  /// Displays an AlertDialog to the user where they can change the local currency of the app
  Future<void> displayCurrencyDialog() async {
    // TODO - clean up this implementation
    int currentOption = getKeyFromValue(ref.read(currencyProvider)) ?? 0;
    int? selectedOption = await showDialog(
      context: context,
      builder: (BuildContext context) {
        return AlertDialog(
          title: const Text(changeCurrencySubtitle),
          content: StatefulBuilder(
            builder: (context, setState) {
              return Column(
                mainAxisSize: MainAxisSize.min,
                children: currencyMapping.entries.map(
                  (entry) {
                    return RadioListTile(
                        title: Text(entry.value.toUpperCase()),
                        value: entry.key,
                        groupValue: currentOption,
                        onChanged: (value) {
                          if (value == null) return;
                          setState(() {
                            currentOption = value;
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
                child: const Text(cancelButtonText)),
            TextButton(
                onPressed: () => Navigator.of(context).pop(currentOption),
                child: const Text(saveButtonText)),
          ],
        );
      },
    );

    if (selectedOption == null) return;

    String selectedOptionText = currencyMapping[selectedOption]!;

    ref.read(currencyProvider.notifier).state = selectedOptionText;

    ref.read(sharedPreferencesProvider).setString(
          currencyKey,
          selectedOptionText,
        );
    // refresh price data
    ref.invalidate(apiProvider);
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
          settingsTitle,
          style: TextStyle(fontWeight: FontWeight.w600),
        ),
      ),
      body: Column(
        mainAxisAlignment: MainAxisAlignment.start,
        children: [
          const HeaderListTile(title: currencyOptionsHeader),
          ListTile(
            title: Text(ref.watch(currencyProvider).toUpperCase()),
            subtitle: const Text(changeCurrencySubtitle),
            onTap: () => displayCurrencyDialog(),
          ),
          const LineDivider(),
          const HeaderListTile(title: appInformationHeader),
          ListTile(
            title: const Text(developerTitle),
            subtitle: const Text(developerSubtitle),
            trailing: const Icon(Icons.open_in_new),
            onTap: () => openUrl(url: portfolioLink),
          ),
          ListTile(
            title: const Text(githubRepositoryTitle),
            subtitle: const Text(sourceCodeSubtitle),
            trailing: const Icon(Icons.open_in_new),
            onTap: () => openUrl(url: githubLink),
          ),
          const LineDivider(),
          const ListTile(
            title: Text(appVersionTitle),
            subtitle: Text(appVersionSubtitle),
          ),
        ],
      ),
    );
  }
}
