import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:minimalbitcoinwidget/constants.dart';
import 'package:minimalbitcoinwidget/models/bitcoin.dart';
import 'package:minimalbitcoinwidget/screens/settings_page.dart';
import 'package:http/http.dart' as http;

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  String sampleText = 'Press to fetch data';

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text(
          appTitle,
          style: TextStyle(fontWeight: FontWeight.w600),
        ),
        actions: [
          IconButton(
            icon: const Icon(Icons.settings),
            tooltip: 'Settings',
            onPressed: () {
              Navigator.push(
                context,
                MaterialPageRoute(builder: (context) => const SettingsPage()),
              );
            },
          )
        ],
      ),
      body: Padding(
        padding: const EdgeInsets.all(8.0),
        child: Column(
          mainAxisSize: MainAxisSize.max,
          children: [
            Card(
              child: InkWell(
                borderRadius: BorderRadius.circular(12.0),
                onTap: () async {
                  String currency = 'usd';
                  final response = await http.get(Uri.parse(
                      'https://api.coingecko.com/api/v3/simple/price?ids=bitcoin&vs_currencies=$currency&include_24hr_change=true&precision=2'));

                  if (response.statusCode == 200) {
                    Map<String, dynamic> jsonData = json.decode(response.body);
                    final bitcoin = Bitcoin.fromJson(jsonData, currency);
                    setState(() {
                      sampleText = bitcoin.toString();
                    });
                  }
                },
                child: SizedBox(
                  height: 200,
                  child: Center(child: Text(sampleText)),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
