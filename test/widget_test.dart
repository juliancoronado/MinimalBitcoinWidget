import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:minimalbitcoinwidget/constants.dart';
import 'package:minimalbitcoinwidget/main.dart';

void main() {
  testWidgets('Test Home Page widgets', (WidgetTester tester) async {
    await tester.pumpWidget(const MinimalBitcoinWidget());
    expect(find.text(appTitle), findsOneWidget);
    expect(find.byIcon(Icons.settings), findsOneWidget);
  });
}
