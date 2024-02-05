import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:minimalbitcoinwidget/constants.dart';
import 'package:minimalbitcoinwidget/providers/api_provider.dart';
import 'package:minimalbitcoinwidget/widgets/loading_price_card.dart';
import 'package:minimalbitcoinwidget/widgets/price_card.dart';
import 'package:minimalbitcoinwidget/screens/settings_page.dart';

class HomePage extends ConsumerWidget {
  const HomePage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    // TODO - clean up this snackbar implementation
    ref.listen<AsyncValue<void>>(
      apiProvider,
      (_, state) => state.whenOrNull(
        error: (_, __) {
          ScaffoldMessenger.of(context).hideCurrentSnackBar();
          final snackBar = SnackBar(
            content: const Text('Too many requests, try again later.'),
            behavior: SnackBarBehavior.floating,
            action: SnackBarAction(
                label: 'Dismiss',
                onPressed: () =>
                    ScaffoldMessenger.of(context).hideCurrentSnackBar()),
          );
          // show snackbar if an error occurred
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
            tooltip: 'Settings',
            onPressed: () {
              Navigator.push(
                context,
                MaterialPageRoute(builder: (context) => const SettingsPage()),
              ).then((value) async {
                // delay to prevent jank
                await Future.delayed(const Duration(milliseconds: 300));
                ref.invalidate(apiProvider);
              });
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
                  onTap: () => ref.invalidate(apiProvider),
                  // TODO - move this into a Price Card widget to clean up the widget tree here
                  child: ref.watch(apiProvider).when(
                        data: (_) => const PriceCard(),
                        skipError: true,
                        skipLoadingOnRefresh: false,
                        error: (err, stack) => const PriceCard(),
                        loading: () => const LoadingPriceCard(),
                      ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
