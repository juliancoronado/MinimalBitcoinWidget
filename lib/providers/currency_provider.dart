import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:minimalbitcoinwidget/constants.dart';
import 'package:minimalbitcoinwidget/providers/shared_preferences_provider.dart';

final currencyProvider = StateProvider<String>((ref) {
  // return the value stored in shared prefs, defaults to 'usd' if it doesn't exist
  return ref.read(sharedPreferencesProvider).getString(currencyKey) ?? 'usd';
});
