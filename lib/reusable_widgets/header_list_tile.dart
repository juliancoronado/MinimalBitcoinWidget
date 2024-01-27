import 'package:flutter/material.dart';

class HeaderListTile extends StatelessWidget {
  final String title;
  const HeaderListTile({super.key, required this.title});

  @override
  Widget build(BuildContext context) {
    return ListTile(
      title: Text(title),
      dense: true,
    );
  }
}
