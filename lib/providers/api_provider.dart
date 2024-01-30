import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:minimalbitcoinwidget/helpers/functions.dart';

final apiProvider = FutureProvider.autoDispose<void>((ref) async {
  ref.onResume(() async {
    await fetchPriceData(ref);
  });

  await fetchPriceData(ref);
});
