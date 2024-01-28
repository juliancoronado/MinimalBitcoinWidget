class Bitcoin {
  final double price;
  final double change24h;

  Bitcoin({
    required this.price,
    required this.change24h,
  });

  factory Bitcoin.fromJson(Map<String, dynamic> json, String currency) {
    if ((json['bitcoin'] as Map<String, dynamic>).isEmpty) {
      throw 'No data returned from API';
    }

    return Bitcoin(
      price: json['bitcoin'][currency],
      change24h: json['bitcoin']['${currency}_24h_change'],
    );
  }

  @override
  String toString() {
    return 'Bitcoin\nPrice: $price\n24h Change: $change24h';
  }
}

// JSON Sample from CoinGecko API
// https://www.coingecko.com/api/documentation
// {
//   "bitcoin": {
//     "usd": 42085.28,
//     "usd_24h_change": 0.6179917629065139
//   }
// }