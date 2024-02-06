import 'dart:convert';

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:home_widget/home_widget.dart';
import 'package:http/http.dart' as http;
import 'package:minimalbitcoinwidget/constants.dart';
import 'package:minimalbitcoinwidget/helpers/url_helper.dart';
import 'package:minimalbitcoinwidget/models/bitcoin.dart';
import 'package:minimalbitcoinwidget/providers/bitcoin_provider.dart';
import 'package:minimalbitcoinwidget/providers/shared_preferences_provider.dart';
import 'package:url_launcher/url_launcher.dart';

Future<void> fetchPriceData(AutoDisposeFutureProviderRef ref) async {
  String currency =
      ref.read(sharedPreferencesProvider).getString(currencyKey) ?? 'usd';
  final response = await http.get(Uri.parse(UrlHelper.url(currency)));

  if (response.statusCode == 200) {
    await Future.delayed(const Duration(seconds: 1));
    Map<String, dynamic> jsonData = json.decode(response.body);
    final bitcoin = Bitcoin.fromJsonApi(jsonData, currency);
    ref.read(bitcoinProvider.notifier).state = bitcoin;
    // store bitcoin data in sharedprefs for local fetching
    ref.read(sharedPreferencesProvider).setString(
          bitcoinKey,
          jsonEncode(bitcoin.toJsonLocal(currency)),
        );
    updateHeadline(
        bitcoin.price.toStringAsFixed(2), bitcoin.change24h.toStringAsFixed(2));
    return;
  } else if (response.statusCode == 429) {
    throw 'Too many requests. Try again later.';
  } else {
    throw response.body;
  }
}

Future<void> openUrl({required String url}) async {
  await launchUrl(
    Uri.parse(url),
    mode: LaunchMode.externalApplication,
  );
}

void updateHeadline(String title, String description) {
  // Save the headline data to the widget
  HomeWidget.saveWidgetData<String>('title', title);
  HomeWidget.saveWidgetData<String>('description', description);
  HomeWidget.updateWidget(
    androidName: 'NewsWidget',
  );
}
