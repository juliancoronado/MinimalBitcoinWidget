import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:minimalbitcoinwidget/constants.dart';
import 'package:minimalbitcoinwidget/providers/api_provider.dart';
import 'package:minimalbitcoinwidget/widgets/error_card.dart';
import 'package:minimalbitcoinwidget/widgets/loading_price_card.dart';
import 'package:minimalbitcoinwidget/widgets/price_card.dart';
import 'package:minimalbitcoinwidget/screens/settings_page.dart';

class HomePage extends ConsumerWidget {
  const HomePage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
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
                  child: ref.watch(apiProvider).when(
                        data: (_) => const PriceCard(),
                        skipLoadingOnRefresh: false,
                        error: (err, stack) {
                          return ErrorCard(error: err as Map<String, dynamic>);
                        },
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
