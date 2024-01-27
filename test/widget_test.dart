import 'package:flutter_test/flutter_test.dart';
import 'package:minimalbitcoinwidget/main.dart';

void main() {
  testWidgets('Initial Tests', (WidgetTester tester) async {
    // Build our app and trigger a frame.
    await tester.pumpWidget(const MinimalBitcoinWidget());
    expect(find.text('Hello, world!'), findsOneWidget);
  });
}
