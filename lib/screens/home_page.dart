import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:minimalbitcoinwidget/constants.dart';
import 'package:minimalbitcoinwidget/providers/api_provider.dart';
import 'package:minimalbitcoinwidget/widgets/price_card.dart';
import 'package:minimalbitcoinwidget/screens/settings_page.dart';

class HomePage extends ConsumerWidget {
  const HomePage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    // listen to changes from apiProvider and handle errors
    ref.listen<AsyncValue<void>>(
      apiProvider,
      (previousState, state) => state.whenOrNull(
        error: (error, _) {
          // prevents loading state from acitivating the snackbar
          if (!(previousState?.isLoading ?? false)) return;

          final snackBar = SnackBar(
            content: Text(error.toString()),
            showCloseIcon: true,
            behavior: SnackBarBehavior.floating,
          );
          // show error snackbar
          ScaffoldMessenger.of(context).showSnackBar(snackBar);
        },
      ),
    );

    return Scaffold(
      appBar: AppBar(
        title: const Text(
          appTitle,
          style: TextStyle(fontWeight: FontWeight.w600),
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.settings),
            tooltip: settingsTitle,
            onPressed: () {
              Navigator.push(
                context,
                MaterialPageRoute(builder: (context) => const SettingsPage()),
              );
            },
          )
        ],
      ),
      body: Padding(
        padding: const EdgeInsets.all(8.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          mainAxisSize: MainAxisSize.max,
          children: [
            Card(
              child: SizedBox(
                height: 200,
                child: InkWell(
                  borderRadius: BorderRadius.circular(12.0),
                  onTap: () {
                    ScaffoldMessenger.of(context).clearSnackBars();
                    ref.invalidate(apiProvider);
                  },
                  child: const PriceWidget(),
                ),
              ),
            ),
            // LineDivider(),
          ],
        ),
      ),
    );
  }
}
