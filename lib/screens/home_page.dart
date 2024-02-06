import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:minimalbitcoinwidget/constants.dart';
import 'package:minimalbitcoinwidget/providers/api_provider.dart';
import 'package:minimalbitcoinwidget/screens/settings_page.dart';
import 'package:minimalbitcoinwidget/widgets/price_card.dart';

class HomePage extends ConsumerStatefulWidget {
  const HomePage({super.key});

  @override
  ConsumerState<HomePage> createState() => _HomePageState();
}

class _HomePageState extends ConsumerState<HomePage>
    with WidgetsBindingObserver {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  @override
  Future<void> didChangeAppLifecycleState(AppLifecycleState state) async {
    // reload price data when the app is resumed from the background
    if (state == AppLifecycleState.resumed) {
      // small delay for better UX
      await Future.delayed(const Duration(milliseconds: 250));
      ref.invalidate(apiProvider);
    }
    super.didChangeAppLifecycleState(state);
  }

  @override
  Widget build(BuildContext context) {
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
