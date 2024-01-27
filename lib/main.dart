import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:minimalbitcoinwidget/constants.dart';
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
        useMaterial3: true,
      ),
      home: const HomePage(title: appTitle),
    );
  }
}

class HomePage extends StatefulWidget {
  const HomePage({super.key, required this.title});

  final String title;

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Theme.of(context).colorScheme.inversePrimary,
        title: Text(widget.title),
      ),
      body: const Center(child: Text('Hello, world!')),
    );
  }
}
