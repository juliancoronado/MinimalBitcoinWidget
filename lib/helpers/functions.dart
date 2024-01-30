import 'dart:convert';

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:http/http.dart' as http;
import 'package:minimalbitcoinwidget/constants.dart';
import 'package:minimalbitcoinwidget/models/bitcoin.dart';
import 'package:minimalbitcoinwidget/providers/bitcoin_provider.dart';
import 'package:minimalbitcoinwidget/providers/shared_preferences_provider.dart';

Future<void> fetchPriceData(AutoDisposeFutureProviderRef ref) async {
  String currency =
      ref.read(sharedPreferencesProvider).getString(currencyKey) ?? 'usd';
  final response = await http.get(Uri.parse(
      'https://api.coingecko.com/api/v3/simple/price?ids=bitcoin&vs_currencies=$currency&include_24hr_change=true&precision=2'));

  if (response.statusCode == 200) {
    await Future.delayed(const Duration(seconds: 1));
    Map<String, dynamic> jsonData = json.decode(response.body);
    final bitcoin = Bitcoin.fromJson(jsonData, currency);
    ref.read(bitcoinProvider.notifier).state = bitcoin;
    return;
  } else {
    // TODO - implement better way to handle this error
    throw json.decode(response.body);
  }
}
