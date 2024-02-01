import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';
import 'package:minimalbitcoinwidget/providers/bitcoin_provider.dart';
import 'package:minimalbitcoinwidget/providers/currency_provider.dart';

class LoadingPriceCard extends ConsumerWidget {
  const LoadingPriceCard({super.key});

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
                child: Image.asset('assets/images/bitcoin_icon.png'),
              ),
              Text(
                ' Bitcoin / ${ref.read(currencyProvider).toUpperCase()}',
                style: const TextStyle(fontSize: 18.0),
              ),
            ],
          ),
          Center(
            child: Text(
              NumberFormat().format(bitcoin.price),
              style:
                  const TextStyle(fontSize: 36.0, fontWeight: FontWeight.bold),
            ),
          ),
          Row(
            mainAxisSize: MainAxisSize.max,
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text('24h: ${bitcoin.change24h.toStringAsFixed(2)}%',
                  style: const TextStyle(fontSize: 18.0)),
              const SizedBox(
                  height: 20,
                  width: 20,
                  child: CircularProgressIndicator(strokeWidth: 2.0)),
            ],
          ),
        ],
      ),
    );
  }
}
