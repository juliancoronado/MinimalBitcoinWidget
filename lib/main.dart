import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:minimalbitcoinwidget/constants.dart';
import 'package:minimalbitcoinwidget/screens/home_page.dart';
// import 'package:shared_preferences/shared_preferences.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // TODO - implement SharedPrefs for storing selected currency
  // final sharedPreferences = await SharedPreferences.getInstance();

  runApp(const MinimalBitcoinWidget());
}

class MinimalBitcoinWidget extends StatelessWidget {
  const MinimalBitcoinWidget({super.key});

  @override
  Widget build(BuildContext context) {
    SystemChrome.setEnabledSystemUIMode(SystemUiMode.edgeToEdge);

    SystemChrome.setSystemUIOverlayStyle(
      const SystemUiOverlayStyle(
        systemNavigationBarColor:
            // draw navigation bar over app (Android)
            Colors.transparent,
      ),
    );

    return MaterialApp(
      title: appTitle,
      themeMode: ThemeMode.system,
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.orange),
        fontFamily: 'Manrope',
        useMaterial3: true,
      ),
      home: const HomePage(),
    );
  }
}
