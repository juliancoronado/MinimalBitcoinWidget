import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:minimalbitcoinwidget/constants.dart';
import 'package:minimalbitcoinwidget/main.dart';
import 'package:minimalbitcoinwidget/providers/shared_preferences_provider.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  testWidgets('Test Home Page widgets', (WidgetTester tester) async {
    SharedPreferences.setMockInitialValues({});

    final sharedPreferences = await SharedPreferences.getInstance();

    await tester.pumpWidget(
      ProviderScope(
        overrides: [
          sharedPreferencesProvider.overrideWithValue(sharedPreferences)
        ],
        child: const MinimalBitcoinWidget(),
      ),
    );
    expect(find.text(appTitle), findsOneWidget);
    expect(find.byIcon(Icons.settings), findsOneWidget);
  });
}
