import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';
import 'package:minimalbitcoinwidget/constants.dart';
import 'package:minimalbitcoinwidget/providers/api_provider.dart';
import 'package:minimalbitcoinwidget/providers/bitcoin_provider.dart';
import 'package:minimalbitcoinwidget/providers/currency_provider.dart';

class PriceWidget extends ConsumerWidget {
  const PriceWidget({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final bitcoin = ref.watch(bitcoinProvider);

    return Padding(
      padding: const EdgeInsets.all(8.0),
      child: Column(
        mainAxisSize: MainAxisSize.max,
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              SizedBox(
                height: 24,
                width: 24,
                child: Image.asset(bitcoinIconAsset),
              ),
              Text(
                ' Bitcoin / ${ref.read(currencyProvider).toUpperCase()}',
                style: const TextStyle(fontSize: 18.0),
              ),
            ],
          ),
          Center(
            child: Text(
              // TODO - implement currency symbols based on users' locale
              // allow users to change this in settings? decimal or comma, symbol position, etc.
              NumberFormat().format(bitcoin.price),
              style:
                  const TextStyle(fontSize: 36.0, fontWeight: FontWeight.bold),
            ),
          ),
          Row(
            mainAxisSize: MainAxisSize.max,
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                '24h: ${bitcoin.change24h.toStringAsFixed(2)}%',
                style: const TextStyle(fontSize: 18.0),
              ),
              ref.watch(apiProvider).when(
                    data: (_) => Container(),
                    skipError: true,
                    skipLoadingOnRefresh: false,
                    error: (err, stack) => Container(),
                    loading: () => const SizedBox(
                      height: 20,
                      width: 20,
                      child: CircularProgressIndicator(strokeWidth: 2.0),
                    ),
                  ),
            ],
          ),
        ],
      ),
    );
  }
}
