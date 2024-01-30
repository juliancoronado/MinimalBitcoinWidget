import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:minimalbitcoinwidget/constants.dart';
import 'package:minimalbitcoinwidget/models/bitcoin.dart';
import 'package:minimalbitcoinwidget/providers/shared_preferences_provider.dart';

final bitcoinProvider = StateProvider<Bitcoin>((ref) {
  String currency =
      ref.read(sharedPreferencesProvider).getString(currencyKey) ?? 'usd';
  return Bitcoin(change24h: 0.00, price: 0.00, currency: currency);
});
