import 'dart:convert';

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:minimalbitcoinwidget/constants.dart';
import 'package:minimalbitcoinwidget/models/bitcoin.dart';
import 'package:minimalbitcoinwidget/providers/shared_preferences_provider.dart';

final bitcoinProvider = StateProvider<Bitcoin>((ref) {
  String currency =
      ref.read(sharedPreferencesProvider).getString(currencyKey) ?? 'usd';
  if (ref.read(sharedPreferencesProvider).containsKey(bitcoinKey)) {
    final bitcoinString =
        ref.read(sharedPreferencesProvider).getString(bitcoinKey) ?? '';
    return Bitcoin.fromJsonLocal(
      jsonDecode(bitcoinString) as Map<String, dynamic>,
      currency,
    );
  }
  return Bitcoin(change24h: 0.00, price: 0.00, currency: currency);
});
