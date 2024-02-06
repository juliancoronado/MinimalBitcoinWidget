class UrlHelper {
  // https://www.coingecko.com/api/documentation
  static url(String currency) {
    // coingecko api
    return 'https://api.coingecko.com/api/v3/simple/price?ids=bitcoin&vs_currencies=$currency&include_24hr_change=true&precision=2';
  }
}
