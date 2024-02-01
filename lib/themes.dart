import 'package:flutter/material.dart';
import 'package:minimalbitcoinwidget/constants.dart';

// CUSTOM MATERIAL 3 LIGHT THEME
final ThemeData lightTheme = ThemeData(
  colorScheme: ColorScheme.fromSeed(
    seedColor: Colors.white,
    brightness: Brightness.light,
  ),
  fontFamily: manropeFontTitle,
  useMaterial3: true,
);

// CUSTOM MATERIAL 3 DARK THEME
final ThemeData darkTheme = ThemeData(
  colorScheme: ColorScheme.fromSeed(
    seedColor: Colors.white,
    brightness: Brightness.dark,
  ),
  fontFamily: manropeFontTitle,
  useMaterial3: true,
);
