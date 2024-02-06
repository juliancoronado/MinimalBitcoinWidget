import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:minimalbitcoinwidget/helpers/functions.dart';

final apiProvider = FutureProvider.autoDispose<void>((ref) async {
  // TODO - investigate if this is needed
  ref.onResume(() async {
    try {
      await fetchPriceData(ref);
    } catch (e) {
      rethrow;
    }
  });

  try {
    await fetchPriceData(ref);
  } catch (e) {
    rethrow;
  }
});
