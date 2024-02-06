class Bitcoin {
  final double price;
  final double change24h;
  final String currency;

  Bitcoin({
    required this.price,
    required this.change24h,
    required this.currency,
  });

  factory Bitcoin.fromJsonApi(Map<String, dynamic> json, String currency) {
    if ((json['bitcoin'] as Map<String, dynamic>).isEmpty) {
      throw 'No data returned from API';
    }

    return Bitcoin(
      price: json['bitcoin'][currency],
      change24h: json['bitcoin']['${currency}_24h_change'],
      currency: currency,
    );
  }

  factory Bitcoin.fromJsonLocal(Map<String, dynamic> json, String currency) {
    return Bitcoin(
      price: json['price'],
      change24h: json['${currency}_24h_change'],
      currency: currency,
    );
  }

  Map<String, dynamic> toJsonLocal(String currency) => {
        'price': price,
        'currency': currency,
        '${currency}_24h_change': change24h,
      };

  @override
  String toString() {
    return 'Bitcoin ($currency)\nPrice: $price\n24h Change: $change24h';
  }
}

// JSON Sample from CoinGecko API
// https://www.coingecko.com/api/documentation
//
// {
//   "bitcoin": {
//     "usd": 42085.28,
//     "usd_24h_change": 0.6179917629065139
//   }
// }