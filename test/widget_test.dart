import 'package:flutter_test/flutter_test.dart';
import 'package:minimalbitcoinwidget/constants.dart';
import 'package:minimalbitcoinwidget/main.dart';

void main() {
  testWidgets('Test AppBar displaying', (WidgetTester tester) async {
    await tester.pumpWidget(const MinimalBitcoinWidget());
    expect(find.text(appTitle), findsOneWidget);
  });
}
